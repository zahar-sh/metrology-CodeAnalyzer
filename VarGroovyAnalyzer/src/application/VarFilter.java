package application;

import analyzer.Variable;

import java.util.List;

class VarFilter {
    final List<Cell> cells;
    int p, c, m, t;

    VarFilter(List<Cell> cells) {
        this.cells = cells;
    }

    public void add(Variable var) {
        if (var.hasFlag(Variable.CONTROLLED)) {
            getOrCreate(c++).setC(var.token);
            return;
        }
        if (var.hasFlag(Variable.MODIFIED)) {
            if (isUsed(var)) {
                getOrCreate(m++).setM(var.token);
                return;
            }
        } else if (isUsed(var)) {
            getOrCreate(p++).setP(var.token);
            return;
        }
        getOrCreate(t++).setT(var.token);
    }

    public void reset() {
        p = c = m = t = 0;
    }

    private boolean isUsed(Variable var) {
        return var.hasFlag(Variable.USED) || var.hasFlag(Variable.INPUT) || var.hasFlag(Variable.OUTPUT);
    }
    private Cell getOrCreate(int i) {
        if (i >= cells.size()) {
            Cell cell = new Cell();
            cells.add(i, cell);
            return cell;
        }
        return cells.get(i);
    }
}
