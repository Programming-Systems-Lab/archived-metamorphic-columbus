/*
	Copyright (c) 2007 Johannes Rieken, All Rights Reserved
	
	This file is part of Modern Jass (http://modernjass.sourceforge.net/).
	
	Modern Jass is free software: you can redistribute it and/or modify
	it under the terms of the GNU Lesser General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.
	
	Modern Jass is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU Lesser General Public License for more details.
	
	You should have received a copy of the GNU Lesser General Public License
	along with Modern Jass.  If not, see <http://www.gnu.org/licenses/>.
*/
package jass.modern.core.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A {@link Map} which realizes a simple cache.
 * <br /><br />
 * The cache is sorted in <em>insertion</em>-order
 * (@see {@link LinkedHashMap}). The {@link #DEFAULT_SIZE default}
 * size is <code>100</code>. Caching can be disabled, 
 * which means its a blackhole (it always returns null, and
 * does not remember new entries). However, existent elements
 * are hold, when the cache is disabled and enabled again.
 *
 * @author riejo
 */
public class CacheMap<K, V> extends LinkedHashMap<K, V> {
	
	public static final int DEFAULT_SIZE = 100;
	
	private int fLimit;
	
	private boolean fEnabled;
	
	public CacheMap() {
		this(DEFAULT_SIZE);
	}
	
	public CacheMap(int size) {
		fLimit = size;
		fEnabled = true;
	}
	
	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
		return fLimit < size();
	}

	public boolean isEnabled() {
		return fEnabled;
	}

	public void setEnabled(boolean enabled) {
		fEnabled = enabled;
	}

	@Override
	public V get(Object key) {
		if(!isEnabled())
			return null;
		
		return super.get(key);
	}

	@Override
	public V put(K key, V value) {
		if(!isEnabled())
			return null;
		
		return super.put(key, value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		if(!isEnabled())
			return;
		
		super.putAll(m);
	}
	
}
