all:
	gcc -c edu_columbia_cs_psl_metamorphic_util_Forker.c -I/System/Library/Frameworks/JavaVM.framework/Versions/A/Headers/
	gcc -dynamiclib -o libforker.jnilib edu_columbia_cs_psl_metamorphic_util_Forker.o -framework JavaVM
	cp libforker.jnilib ../
clean:
	rm libforker.jnilib edu_columbia_cs_psl_metamorphic_util_Forker.o
