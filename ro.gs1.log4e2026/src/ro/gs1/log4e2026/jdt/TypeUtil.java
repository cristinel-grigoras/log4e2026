package ro.gs1.log4e2026.jdt;

import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;

/**
 * Utility class for type resolution and manipulation.
 */
public final class TypeUtil {

    private TypeUtil() {
    }

    /**
     * Creates a JDTTypeBean from a Type node.
     */
    public static JDTTypeBean getTypeBean(Type type) {
        if (type == null) {
            return null;
        }

        JDTTypeBean bean = new JDTTypeBean();

        if (type instanceof PrimitiveType) {
            PrimitiveType pt = (PrimitiveType) type;
            String name = pt.getPrimitiveTypeCode().toString();
            bean.setName(name);
            bean.setQualifiedName(name);
            bean.setPrimitive(true);
        } else if (type instanceof SimpleType) {
            SimpleType st = (SimpleType) type;
            bean.setName(st.getName().getFullyQualifiedName());
            ITypeBinding binding = st.resolveBinding();
            if (binding != null) {
                bean.setQualifiedName(binding.getQualifiedName());
                bean.setPackageName(binding.getPackage() != null ? binding.getPackage().getName() : null);
            } else {
                bean.setQualifiedName(bean.getName());
            }
        } else if (type instanceof ArrayType) {
            ArrayType at = (ArrayType) type;
            JDTTypeBean elementBean = getTypeBean(at.getElementType());
            if (elementBean != null) {
                bean.setName(elementBean.getName());
                bean.setQualifiedName(elementBean.getQualifiedName());
                bean.setPrimitive(elementBean.isPrimitive());
            }
            bean.setArray(true);
            bean.setArrayDimensions(at.getDimensions());
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            return getTypeBean(pt.getType());
        } else {
            bean.setName(type.toString());
            bean.setQualifiedName(type.toString());
        }

        return bean;
    }

    /**
     * Gets the simple type name from a type.
     */
    public static String getSimpleTypeName(Type type) {
        JDTTypeBean bean = getTypeBean(type);
        return bean != null ? bean.getName() : null;
    }

    /**
     * Gets the qualified type name from a type.
     */
    public static String getQualifiedTypeName(Type type) {
        JDTTypeBean bean = getTypeBean(type);
        return bean != null ? bean.getQualifiedName() : null;
    }

    /**
     * Checks if a type is a primitive type.
     */
    public static boolean isPrimitive(Type type) {
        return type instanceof PrimitiveType;
    }

    /**
     * Checks if a type is an array type.
     */
    public static boolean isArray(Type type) {
        return type instanceof ArrayType;
    }

    /**
     * Checks if a type is void.
     */
    public static boolean isVoid(Type type) {
        if (type instanceof PrimitiveType) {
            return ((PrimitiveType) type).getPrimitiveTypeCode() == PrimitiveType.VOID;
        }
        return false;
    }

    /**
     * Gets the type name suitable for logging output.
     */
    public static String getLoggableTypeName(Type type) {
        if (type == null) {
            return "void";
        }
        JDTTypeBean bean = getTypeBean(type);
        if (bean == null) {
            return type.toString();
        }
        StringBuilder sb = new StringBuilder(bean.getName());
        if (bean.isArray()) {
            for (int i = 0; i < bean.getArrayDimensions(); i++) {
                sb.append("[]");
            }
        }
        return sb.toString();
    }

    /**
     * Checks if a type is a common logging-related exception.
     */
    public static boolean isLoggableException(String typeName) {
        if (typeName == null) {
            return false;
        }
        return typeName.endsWith("Exception") || typeName.endsWith("Error") || typeName.equals("Throwable");
    }

    /**
     * Gets the default value string for a type (for log output).
     */
    public static String getDefaultValueString(Type type) {
        if (type == null || isVoid(type)) {
            return "";
        }
        if (isPrimitive(type)) {
            PrimitiveType pt = (PrimitiveType) type;
            PrimitiveType.Code code = pt.getPrimitiveTypeCode();
            if (code == PrimitiveType.BOOLEAN) {
                return "false";
            } else if (code == PrimitiveType.CHAR) {
                return "'\\0'";
            } else if (code == PrimitiveType.FLOAT || code == PrimitiveType.DOUBLE) {
                return "0.0";
            } else {
                return "0";
            }
        }
        return "null";
    }
}
