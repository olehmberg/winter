/*
 * Copyright (c) 2017 Data and Web Science Group, University of Mannheim, Germany (http://dws.informatik.uni-mannheim.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes;

import de.uni_mannheim.informatik.dws.winter.preprocessing.units.Quantity;
import de.uni_mannheim.informatik.dws.winter.preprocessing.units.Unit;
import de.uni_mannheim.informatik.dws.winter.preprocessing.units.UnitCategory;

/**
 *
 * @author Alexander Brinkmann (albrinkm@mail.uni-mannheim.de)
 * 
 * 
 */
public class ColumnDetectionType extends ColumnType {
    
    private UnitCategory category;
    private Quantity quantity;
    
    public ColumnDetectionType(DataType type, Unit unit, UnitCategory category, Quantity quantity) {
        super(type, unit);
        this.category = category;
        this.quantity = quantity;
    }

    
    /**
     * @return the unit category
     */
    public UnitCategory getUnitCategory() {
        return category;
    }
    
    /**
     * @return the unit category
     */
    public Quantity getQuantity() {
        return quantity;
    }
    
}
