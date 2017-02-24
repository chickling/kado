package com.chickling.dbselect.module;



/**
 * @author: aj65
 */
public class Row {
    public static class Cell {
        private ColumnType type;
        private String name;
        private String value;

        public Cell(ColumnType type, String name, String value) {
            this.type = type;
            this.name = name;
            this.value = value;
        }

        public ColumnType getType() {
            return type;
        }

        public void setType(ColumnType type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString(){
            return String.format("{type:%s, name:%s, value:%s}",type,name,value);
        }
    }

    private ColumnType[]  types;
    private String[]      columns;
    private String[]      values;

    public Row() {}

    public Row(ColumnType[] types,String[] columns, String[] values) {
        this.types = types;
        this.columns = columns;
        this.values = values;
    }

    public String[] getColumns() {
        return columns;
    }

    public void setColumns(String[] columns) {
        this.columns = columns;
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String[] values) {
        this.values = values;
    }

    public ColumnType[] getTypes() {
        return types;
    }

    public void setTypes(ColumnType[] types) {
        this.types = types;
    }

    /**
     * @param name
     * @return  -1 if no related
     */
    private int findIndex(String name){
        int ret = -1;
        if( null!=columns )
        {
            int index = 0;
            for(String n:columns)
            {
                if(name.equals(n))
                    break;
                index ++;
            }
            ret = index!=-1?index:-1;
        }

        return ret;
    }

    /**
     * get the n-th Cell of this row
     * @param n the index of column, start from 0
     * @return the related Cell,
     *         null if n is out-of-range or columns is null in Row
     */
    public Cell getCell(int n){
        Cell ret = null;
        if( null!=columns )
        {
            if(columns.length > n)
              ret = new Cell(types[n],columns[n],values[n]);
        }

        return ret;
    }

    /**
     * get column which column's name equals to the input parameter name
     * @param name the name of column
     * @return the column
     *         null if find no related column or columns is null in Row
     */
    public Cell getCell(String name){
        Cell ret = null;
        if( null!=columns )
        {
            int index = findIndex(name);
            if( -1!=index)
                ret = new Cell(types[index],columns[index],values[index]);
        }

        return ret;
    }

    @Override
    public String toString(){
        String ret = null;
        if( null!=types )
        {
            StringBuilder sb = new StringBuilder(0x1FF);
            for(String col:columns)
                sb.append(String.format("%-25s",col));

            sb.append("\n");
            for(String v: values)
                sb.append(String.format("%-25s",v));
            ret = sb.toString();
        }
        return ret;
    }
}
