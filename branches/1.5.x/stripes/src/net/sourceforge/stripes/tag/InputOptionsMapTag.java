/* Copyright 2007 Aaron Porter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.stripes.tag;

import java.util.Map;

import javax.servlet.jsp.JspException;

import net.sourceforge.stripes.tag.InputOptionsCollectionTag;

/**
 * <p>Extracts the {@link java.util.Set} of {@link java.util.Map.Entry} from the
 * specified {@link java.util.Map} and uses it as the {@link java.util.Collection}
 * for the superclass {@link net.sourceforge.stripes.tag.InputOptionsCollectionTag}.</p>
 * 
 * <p>The value and label parameters will be set to "key" and "value" respectively
 * if they are null.</p>
 *  
 * @author Aaron Porter
 *
 */
public class InputOptionsMapTag extends InputOptionsCollectionTag
{
	private Map<? extends Object,? extends Object> map;

	/**
	 * <p>Returns the {@link java.util.Map} that was passed in via setMap().</p>
	 * 
	 * @return the {@link java.util.Map} passed in via setMap().
	 */
	public Map<? extends Object, ? extends Object> getMap() {
		return map;
	}

    /**
     * <p>This function simply passes the result of Map.entrySet()
     * as the collection to be used by the superclass and sets the value and label
     * variables if they have not already been set.</p>
     *
     * @param map a Map
     */
	public void setMap(Map<? extends Object, ? extends Object> map)	{
		this.map = map;
		
		setCollection(map.entrySet());
		
		if (getValue() == null)
			setValue("key");
		
		if (getLabel() == null)
			setLabel("value");
	}
	
	/**
	 * Calls super.doEndTag() and cleans up instance variables so this instance
	 * may be reused.
	 */
    @Override
    public int doEndTag() throws JspException {
    	int result = super.doEndTag();
    	
    	this.map = null;
    	
    	return result;
    }
}
