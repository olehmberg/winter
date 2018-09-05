package de.uni_mannheim.informatik.dws.winter.preprocessing.units;

import java.util.ArrayList;
import java.util.List;

public class UnitCategory {

    private String name;
    private List<Unit> units = new ArrayList<>();
    
    public UnitCategory(String name){
    	this.name = name;
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @return the list of units
     */
	public List<Unit> getUnits() {
		return units;
	}
	
	/**
	 * @param units list of units belonging to the unit category
	 */
	public void setUnits(List<Unit> units) {
		this.units = units;
	}
	
	/**
	 * 
	 * @param unit Adds the unit to the list of units
	 */
	public void addUnit(Unit unit) {
		this.units.add(unit);
	}
}
