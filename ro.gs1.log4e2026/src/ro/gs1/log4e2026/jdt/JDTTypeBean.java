package ro.gs1.log4e2026.jdt;

/**
 * Bean class to hold type information.
 */
public class JDTTypeBean {

    private String name;
    private String qualifiedName;
    private String packageName;
    private boolean primitive;
    private boolean array;
    private int arrayDimensions;

    public JDTTypeBean() {
    }

    public JDTTypeBean(String name) {
        this.name = name;
        this.qualifiedName = name;
        this.primitive = isPrimitiveType(name);
    }

    public JDTTypeBean(String name, String qualifiedName) {
        this.name = name;
        this.qualifiedName = qualifiedName;
        if (qualifiedName != null && qualifiedName.contains(".")) {
            int lastDot = qualifiedName.lastIndexOf('.');
            this.packageName = qualifiedName.substring(0, lastDot);
        }
        this.primitive = isPrimitiveType(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean isPrimitive() {
        return primitive;
    }

    public void setPrimitive(boolean primitive) {
        this.primitive = primitive;
    }

    public boolean isArray() {
        return array;
    }

    public void setArray(boolean array) {
        this.array = array;
    }

    public int getArrayDimensions() {
        return arrayDimensions;
    }

    public void setArrayDimensions(int arrayDimensions) {
        this.arrayDimensions = arrayDimensions;
        this.array = arrayDimensions > 0;
    }

    /**
     * Checks if a type name represents a primitive type.
     */
    public static boolean isPrimitiveType(String typeName) {
        if (typeName == null) {
            return false;
        }
        return switch (typeName) {
            case "boolean", "byte", "char", "short", "int", "long", "float", "double", "void" -> true;
            default -> false;
        };
    }

    /**
     * Gets the wrapper class for a primitive type.
     */
    public static String getWrapperClass(String primitiveType) {
        if (primitiveType == null) {
            return null;
        }
        return switch (primitiveType) {
            case "boolean" -> "Boolean";
            case "byte" -> "Byte";
            case "char" -> "Character";
            case "short" -> "Short";
            case "int" -> "Integer";
            case "long" -> "Long";
            case "float" -> "Float";
            case "double" -> "Double";
            case "void" -> "Void";
            default -> primitiveType;
        };
    }

    @Override
    public String toString() {
        return "JDTTypeBean{" +
                "name='" + name + '\'' +
                ", qualifiedName='" + qualifiedName + '\'' +
                ", primitive=" + primitive +
                ", array=" + array +
                '}';
    }
}
