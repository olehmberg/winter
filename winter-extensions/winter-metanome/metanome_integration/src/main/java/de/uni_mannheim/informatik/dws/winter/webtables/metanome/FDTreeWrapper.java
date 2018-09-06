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
package de.uni_mannheim.informatik.dws.winter.webtables.metanome;

import de.metanome.algorithms.hyfd.structures.FDTree;
import de.metanome.algorithms.hyfd.structures.FDTreeElement;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;
import de.uni_mannheim.informatik.dws.winter.webtables.TableColumn;
import java.util.*;
import org.apache.lucene.util.OpenBitSet;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class FDTreeWrapper {

    private FDTree fds;

    TableColumn[] idxToAtt;
    Map<TableColumn, Integer> attToIdx;

    public FDTreeWrapper(Collection<TableColumn> attributes) {
        fds = new FDTree(attributes.size(), -1);
        idxToAtt = new TableColumn[attributes.size()];
        attToIdx = new HashMap<>();

        int idx=0;
        for(TableColumn c : attributes) {
            idxToAtt[idx]=c;
            attToIdx.put(c,idx);
            idx++;
        }        
    }

    public void setFunctionalDependencies(Map<Set<TableColumn>,Set<TableColumn>> functionalDependencies) {
        for(Pair<Set<TableColumn>,Set<TableColumn>> fd : Pair.fromMap(functionalDependencies)) {
            addMinimalFunctionalDependency(fd);
        }
    }

    public void addMostGeneralDependencies() {
        fds.addMostGeneralDependencies();
    }
    protected OpenBitSet getBitSet(Set<TableColumn> columnCombination) {
        OpenBitSet set = new OpenBitSet(fds.getNumAttributes());

        for(TableColumn c : columnCombination) {
            set.set(attToIdx.get(c));
        }

        return set;
    }

    public void addMinimalFunctionalDependency(Pair<Set<TableColumn>,Set<TableColumn>> fd) {
        OpenBitSet lhs = getBitSet(fd.getFirst());
        OpenBitSet rhs = getBitSet(fd.getSecond());
        
        fds.addFunctionalDependency(lhs,rhs);
    }

    public void removeNonFunctionalDependency(Pair<Set<TableColumn>,Set<TableColumn>> nonFd) {
        OpenBitSet lhs = getBitSet(nonFd.getFirst());

        for(TableColumn c : nonFd.getSecond()) {
            fds.removeFunctionalDependency(lhs, attToIdx.get(c));
        }
    }

    public boolean containsFdOrGeneralisation(Set<TableColumn> lhs, TableColumn rhs) {
        return fds.containsFdOrGeneralization(getBitSet(lhs), attToIdx.get(rhs));
    }

    public Map<Set<TableColumn>,Set<TableColumn>> getFunctionalDependencies() {
        Map<Set<TableColumn>,Set<TableColumn>> result = new HashMap<>();
        listFDs(fds, new OpenBitSet(), result);
        return result;
    }

    protected void listFDs(FDTreeElement fds, OpenBitSet lhs, Map<Set<TableColumn>,Set<TableColumn>> result) {
        Set<TableColumn> lhsIDs = new HashSet<>();
        for(int i = lhs.nextSetBit(0); i >= 0; i = lhs.nextSetBit(i + 1)) {
            lhsIDs.add(idxToAtt[i]);
        }

        Set<TableColumn> rhsIDs = new HashSet<>();
        for(int i = fds.getFds().nextSetBit(0); i >= 0; i = fds.getFds().nextSetBit(i + 1)) {
            rhsIDs.add(idxToAtt[i]);
        }                
        
        rhsIDs = new HashSet<>(Q.without(rhsIDs, lhsIDs));
        if(rhsIDs.size()>0) {
            result.put(lhsIDs, rhsIDs);
        }

        if(fds.getChildren()!=null) {
            for(int childAttr = 0; childAttr < fds.getNumAttributes(); childAttr++) {
                FDTreeElement element = fds.getChildren()[childAttr];
                if(element!=null) {
                    lhs.set(childAttr);
                    listFDs(element, lhs, result);
                    lhs.clear(childAttr);
                }
            }
        }
    }

    public void removeGeneralisations() {
        fds.filterGeneralizations();
    }

    public void minimise() {
        fds.minimize();
    }
}