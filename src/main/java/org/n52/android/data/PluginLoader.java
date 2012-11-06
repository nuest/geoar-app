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
package org.n52.android.data;


/**
 * 
 * @author Arne de Wall
 * 
 */
public class PluginLoader {

//	private static Map<Class<?>, PluginUpdateListener> listeners = Collections
//			.synchronizedMap(new WeakHashMap<Class<?>, PluginUpdateListener>());
//
//	interface PluginUpdateListener {
//		void pluginUpdate(PluginHolder holder);
//
//		void refreshViewOnMainThread();
//	}
//
//	public static void addPluginUpdateListener(PluginUpdateListener listener,
//			Class<?> holderClazz) {
//		listeners.put(holderClazz, listener);
//	}
//
//	public static void removePluginUpdateListener(PluginUpdateListener listener) {
//		listeners.remove(listener);
//	}
//
//	static abstract class PluginHolder {
//		String identification;
//		Long version;
//		String description;
//
//		@Override
//		public int hashCode() {
//			final int prime = 31;
//			int result = 1;
//			result = prime * result;
//			result = prime
//					* result
//					+ ((identification == null) ? 0 : identification.hashCode());
//			result = prime * result
//					+ ((version == null) ? 0 : version.hashCode());
//			return result;
//		}
//
//		@Override
//		public boolean equals(Object obj) {
//			if (this == obj)
//				return true;
//			if (obj == null)
//				return false;
//			if (!(obj instanceof DatasourceHolder))
//				return false;
//			DatasourceHolder other = (DatasourceHolder) obj;
//			if (identification == null) {
//				if (other.identification != null)
//					return false;
//			} else if (!identification.equals(other.identification))
//				return false;
//			if (version == null) {
//				if (other.version != null)
//					return false;
//			} else if (!version.equals(other.version))
//				return false;
//			return true;
//		}
//
//		@Override
//		public String toString() {
//			return identification + "-" + version;
//		}
//	};
//
//	class AddPluginCallback {
//		void addPlugin(PluginHolder holder) {
//			if (holder instanceof CodebaseHolder) {
//				if (dsHolders.contains(holder))
//					return;
//				cbHolders.add((CodebaseHolder) holder);
//			} else {
//				dsHolders.add((DatasourceHolder) holder);
//			}
//			PluginUpdateListener l = listeners.get(holder.getClass());
//			if (l != null)
//				l.pluginUpdate(holder);
//		}
//
//		void updateListener() {
//			for (PluginUpdateListener l : listeners.values())
//				l.refreshViewOnMainThread();
//		}
//	}
//
//	private final FactoryLoader factoryLoader;
//	private final CodebaseDownloader codebaseDownloader;
//	private Set<DatasourceHolder> dsHolders = new HashSet<DatasourceHolder>();
//	private Set<CodebaseHolder> cbHolders = new HashSet<CodebaseHolder>();
//
//	public PluginLoader(ClassLoader classLoader, Context context) {
//		this.factoryLoader = new FactoryLoader(classLoader, context);
//		this.codebaseDownloader = new CodebaseDownloader();
//		refresh();
//	}
//
//	public void updateCodebase() {
//		factoryLoader.reloadFactorys(new AddPluginCallback());
//	}
//
//	void refresh() {
//		this.factoryLoader.reloadFactorys(new AddPluginCallback());
//		this.codebaseDownloader.availableDatasources(new AddPluginCallback());
//	}
//
//	public DataSourceAbstractFactory getFactoryById(String id) {
//		for (DatasourceHolder holder : dsHolders) {
//			if (holder.identification.equals(id))
//				return holder.factory;
//		}
//		try {
//			throw new FileNotFoundException("DataSourceAbstractFactory for "
//					+ id + " not found");
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}

}