/**
 * Copyright 2012 52�North Initiative for Geospatial Open Source Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.android.ar.view;

import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import org.n52.android.R;
import org.n52.android.ar.view.gl.ARSurfaceViewRenderer.OpenGLCallable;
import org.n52.android.ar.view.gl.GLESCamera;
import org.n52.android.newdata.SpatialEntity;
import org.n52.android.newdata.Visualization;
import org.n52.android.newdata.Visualization.FeatureVisualization;
import org.n52.android.newdata.vis.DataSourceVisualization.DataSourceVisualizationCanvas;
import org.n52.android.tracking.location.LocationHandler;
import org.n52.android.view.geoar.gl.mode.RenderFeature2;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

public class ARObject implements OpenGLCallable {

	private static float getScaleByDistance(float distance) {
		// XXX TODO FIXME reworking scaling function
		int x = org.n52.android.view.geoar.Settings.BUFFER_MAPINTERPOLATION;
		if (distance > x) {
			return 0.5f;
		}
		float scale = 1 - (distance / (x * 2));
		return Math.max(0.5f, scale);
	}

	private static Paint distancePaint = new Paint();
	static {
		distancePaint.setAntiAlias(true);
		distancePaint.setColor(Color.GRAY);
		distancePaint.setAlpha(100);
	}

	/** Model Matrix of this feature */
	private final float[] modelMatrix = new float[16];
	/** Model view Matrix of this feature */
	private final float[] modelViewMatrix = new float[16];
	/** Model-View-Projection Matrix of our feature */
	private final float[] mvpMatrix = new float[16];
	/** temporary Matrix for caching */
	private final float[] tmpMatrix = new float[16];

	private float distanceTo;
	private float featureDetailsScale;
	private final float[] newPosition = new float[4];
	private final float[] screenCoordinates = new float[3];

	private volatile boolean isInFrustum = false;


	// XXX Why mapping by Class? Compatible with multiinstancedatasources?
	// private final Map<Class<? extends ItemVisualization>, VisualizationLayer>
	// visualizationLayers = new HashMap<Class<? extends ItemVisualization>,
	// VisualizationLayer>();

	private final SpatialEntity entity;
	private DataSourceVisualizationCanvas canvasFeature;
	private List<RenderFeature2> renderFeatures;
	private FeatureVisualization visualization;
	private View featureDetailView;
	private Bitmap featureDetailBitmap;

	// TODO FIXME XXX task: ARObject gains most functionalities of RenderFeature
	// (-> RenderFeature to be more optional)
	public ARObject(SpatialEntity entity,
			Visualization.FeatureVisualization visualization,
			List<RenderFeature2> features,
			DataSourceVisualizationCanvas canvasFeature) {
		this.entity = entity;
		this.renderFeatures = features;
		this.canvasFeature = canvasFeature;
		this.visualization = visualization;

		onLocationUpdate(LocationHandler.getLastKnownLocation());
	}

	public ARObject(SpatialEntity entity,
			Visualization.FeatureVisualization visualization,
			List<RenderFeature2> features,
			DataSourceVisualizationCanvas canvasFeature, View featureDetailView) {
		this.entity = entity;
		this.renderFeatures = features;
		this.canvasFeature = canvasFeature;
		this.visualization = visualization;

		if (featureDetailView != null) {
			this.featureDetailView = featureDetailView;

			this.featureDetailView.setLayoutParams(new LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT));

			this.featureDetailView.setDrawingCacheEnabled(true);
			this.featureDetailView.measure(
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

			this.featureDetailView.layout(0, 0,
					this.featureDetailView.getMeasuredWidth(),
					this.featureDetailView.getMeasuredHeight());

			this.featureDetailView.buildDrawingCache(true);

			featureDetailBitmap = Bitmap.createBitmap(this.featureDetailView
					.getDrawingCache());
			this.featureDetailView.setDrawingCacheEnabled(false);

		}

		onLocationUpdate(LocationHandler.getLastKnownLocation());
	}

	@Override
	public void onPreRender() {
		// TODO Auto-generated method stub

	}

	@Override
	public void render(final float[] projectionMatrix,
			final float[] viewMatrix, final float[] parentMatrix,
			final float[] lightPosition) {

		/** set the matrices to identity matrix */
		Matrix.setIdentityM(modelMatrix, 0);
		Matrix.setIdentityM(modelViewMatrix, 0);
		Matrix.setIdentityM(mvpMatrix, 0);
		Matrix.setIdentityM(tmpMatrix, 0);

		// TODO i think position[0] must be translated negatively -> Check
		Matrix.translateM(modelMatrix, 0, -newPosition[0], newPosition[1],
				newPosition[2]);

		if (parentMatrix != null) {
			Matrix.multiplyMM(tmpMatrix, 0, parentMatrix, 0, modelMatrix, 0);
			System.arraycopy(tmpMatrix, 0, modelMatrix, 0, 16);
			Matrix.setIdentityM(tmpMatrix, 0);
		}

		Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
		Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);

		// TODO XXX FIXME frustum test
		if (newPosition != null) {
			float[] vec = new float[] { 0, 0, 0, 1 };
			Matrix.multiplyMV(vec, 0, modelMatrix, 0, vec, 0);
			if (!GLESCamera.frustumCulling(vec)) {
				isInFrustum = false;
				return;
			}
			/** object is in Frustum - update screen coordinates */
			isInFrustum = true;
			updateScreenCoordinates();
		}
		// isInFrustum = true;

		// TODO XXX FIXME are just active visualizations called !? -> check
		// for (VisualizationLayer layer : visualizationLayers.values()) {
		for (RenderFeature2 feature : renderFeatures) {
			feature.render(mvpMatrix, modelViewMatrix, lightPosition);
		}
		// }
	}

	public void initializeRendering() {
		for (RenderFeature2 feature : renderFeatures) {
			feature.onCreateInGLESThread();
		}
	}

	public void onItemClicked(Context context) {
		View featureView = visualization.getFeatureView(entity, null, null,
				context);
		if (featureView != null) {
			String title = visualization.getTitle(entity);
			if (title == null || title.isEmpty()) {
				title = "";
			}
			String message = visualization.getDescription(entity);
			if (message == null || message.isEmpty()) {
				message = "";
			}
			Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(title).setMessage(message)
					.setNeutralButton(R.string.cancel, null)
					.setView(featureView);
			builder.create().show();
		}
	}

	private final void updateScreenCoordinates() {
		float[] screenPos = new float[3];
		// TODO FIXME XXX i think newPosition[2] has to be negative
		int result = GLU.gluProject(-newPosition[0], newPosition[1],
				newPosition[2], modelMatrix, 0, GLESCamera.projectionMatrix, 0,
				GLESCamera.viewportMatrix, 0, screenPos, 0);

		if (result == GL10.GL_TRUE) {
			screenCoordinates[0] = screenPos[0];
			screenCoordinates[1] = GLESCamera.viewportMatrix[3] - screenPos[1];
		}
	}

	public boolean thisObjectHitted(float cx, float cy) {
		float dx = screenCoordinates[0] - cx;
		float dy = screenCoordinates[1] - cy;
		float length = (float) Math.sqrt(dx * dx - dy * dy);

		if (length <= 20) {
			return true;
		} else {
			return false;
		}
	}

	public void onLocationUpdate(Location location) {
		if (entity == null || location == null)
			return;

		final double longitude = entity.getLongitude();
		final double latitude = entity.getLatitude();
		int altitude = entity.getAltitude();

		/** calc the distance XXX */
		final float[] x = new float[1];
		Location.distanceBetween(location.getLatitude(),
				location.getLongitude(), latitude, longitude, x);
		distanceTo = x[0];
		x[0] = 0;

		/** set scaling */
		this.featureDetailsScale = getScaleByDistance(distanceTo);

		/** just the distance -> length 1 */
		Location.distanceBetween(location.getLatitude(),
				location.getLongitude(), location.getLatitude(), longitude, x);

		/** just the distance -> length 1 */
		final float[] z = new float[1];
		Location.distanceBetween(location.getLatitude(),
				location.getLongitude(), latitude, location.getLongitude(), z);

		// correct the direction according to the poi location, because we just
		// get the distance in x and z direction
		if (location.getLongitude() < longitude)
			x[0] *= -1;
		if (location.getLatitude() < latitude)
			z[0] *= -1;
		if (altitude == 0)
			altitude = (int) location.getAltitude();
		// testen

		newPosition[0] = x[0];
		newPosition[1] = (float) (altitude - location.getAltitude());
		// FIXME XXX TODO and here the third position has to be negative i think
		newPosition[2] = z[0];

		for (RenderFeature2 renderFeature : renderFeatures)
			renderFeature.setRelativePosition(newPosition);

		this.newPosition[0] = newPosition[0]; // - GLESCamera.cameraPosition[0];
		this.newPosition[1] = newPosition[1] - GLESCamera.cameraPosition[1];
		this.newPosition[2] = newPosition[2]; // - GLESCamera.cameraPosition[2];
	}

	public void renderCanvas(Paint poiRenderer, Canvas canvas) {
		// FIXME TODO XXX distanceTo has to be in the Settings
		if (isInFrustum) {
			/**
			 * scale the direction indicator of the ARObject with the distance
			 * scale
			 */
			canvas.save(Canvas.MATRIX_SAVE_FLAG);

			canvas.scale(featureDetailsScale, featureDetailsScale,
					screenCoordinates[0], screenCoordinates[1]);

			canvasFeature.onRender(screenCoordinates[0], screenCoordinates[1],
					canvas);
			canvas.restore();

//			canvas.drawRect(screenCoordinates[0] - 20,
//					screenCoordinates[1] - 10, screenCoordinates[0] + 20,
//					screenCoordinates[1] + 10, distancePaint);

			/** draw the featureDetailsView bitmap */
			if (featureDetailBitmap != null)
				canvas.drawBitmap(featureDetailBitmap, screenCoordinates[0],
						screenCoordinates[1], null);
		}
	}

	public void setLightPosition(float[] lightPosInEyeSpace) {
		// GLES20.glUniform3f(renderfe, lightPosInEyeSpace[0],
		// lightPosInEyeSpace[1], lightPosInEyeSpace[2]);

	}

}
