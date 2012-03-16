public class Method
{
    public String permission = "";
    public String returnType;
    public String name;
    public String params;
    public String throwsPart = "";

    public boolean isStatic = false;
    public boolean isFinal = false;
    public boolean isSynchronized = false;
    public boolean isExtern = false;
    public boolean isInline = false;

    public String toString()
    {
	return permission + " " + returnType + " " + name + " " + params + " " + throwsPart;
    }
}