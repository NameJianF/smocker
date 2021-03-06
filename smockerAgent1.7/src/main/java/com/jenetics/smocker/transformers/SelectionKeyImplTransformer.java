package com.jenetics.smocker.transformers;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;

public class SelectionKeyImplTransformer {

	public byte[] transform(byte[] classfileBuffer)
			throws IOException, NotFoundException, CannotCompileException {
		byte[] byteCode;
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));

		defineNewMethod(ctClass);

		byteCode = ctClass.toBytecode();
		ctClass.detach();
		ctClass.defrost();
		return byteCode;
	}

	private void defineNewMethod(CtClass ctClass) throws CannotCompileException, NotFoundException {
		ctClass.setModifiers(Modifier.PUBLIC);
		
		CtField readyOpsField = ctClass.getDeclaredField("readyOps");
		readyOpsField.setModifiers(Modifier.PUBLIC);
		
		ctClass.getDeclaredField("selector").setModifiers(Modifier.PUBLIC);
	
		
		final String getSelectorBody = "public Object getSelector () {"
				+ " try{" 
				+ " 	return $0.selector;"
				+ "} catch (Throwable t) "
				+ "{ "
				+ "     t.printStackTrace(); "
				+ "     throw t; "
				+ "}" 
				+ "}";
		
		CtMethod getSelector = CtNewMethod.make(getSelectorBody, ctClass);
		ctClass.addMethod(getSelector);
		
		
		
	}

	

}
