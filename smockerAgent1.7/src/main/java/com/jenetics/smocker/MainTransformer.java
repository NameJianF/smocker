package com.jenetics.smocker;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import com.jenetics.smocker.transformers.AbstractInterruptibleChannelTransformer;
import com.jenetics.smocker.transformers.AbstractSelectableChannelTransformer;
import com.jenetics.smocker.transformers.NioEventLoopTransformer;
import com.jenetics.smocker.transformers.SSLSocketImplTransformer;
import com.jenetics.smocker.transformers.SelectionKeyImplTransformer;
import com.jenetics.smocker.transformers.SelectionKeyTransformer;
import com.jenetics.smocker.transformers.SocketChannelImplTransformer;
import com.jenetics.smocker.transformers.SocketTransformer;
import com.jenetics.smocker.transformers.WindowsSelectorImplTransformer;
import com.jenetics.smocker.util.MessageLogger;

/**
 * Main transformer
 * @author igolus
 *
 */
public class MainTransformer implements ClassFileTransformer {
	
	
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		byte[] byteCode = classfileBuffer;
		try {
			if (className != null && className.equals("sun/security/ssl/SSLSocketImpl")) {
				byteCode = new SSLSocketImplTransformer().transform(classfileBuffer);
			}
			
			if (className != null && className.equals("java/net/Socket")) {
				byteCode = new SocketTransformer().transform(classfileBuffer);
			}
			
			if (className != null && className.equals("java/net/URLConnection")) {
				byteCode = new SocketTransformer().transform(classfileBuffer);
			}
			
			if (className != null && className.equals("sun/nio/ch/SocketChannelImpl")) {
				byteCode = new SocketChannelImplTransformer().transform(classfileBuffer);
			}
			
			if (className != null && className.equals("java/nio/channels/spi/AbstractInterruptibleChannel")) {
				byteCode = new AbstractInterruptibleChannelTransformer().transform(classfileBuffer);
			}
			
			if (className != null && className.equals("java/nio/channels/spi/AbstractSelectableChannel")) {
				byteCode = new AbstractSelectableChannelTransformer().transform(classfileBuffer);
			}
			
			if (className != null && className.equals("sun/nio/ch/SelectionKeyImpl")) {
				byteCode = new SelectionKeyImplTransformer().transform(classfileBuffer);
			}
			
			if (className != null && className.equals("java/nio/channels/SelectionKey")) {
				byteCode = new SelectionKeyTransformer().transform(classfileBuffer);
			}
			
			if (className != null && className.equals("io/netty/channel/nio/NioEventLoop")) {
				byteCode = new NioEventLoopTransformer().transform(classfileBuffer);
			}
			
			if (className != null && (className.equals("sun/nio/ch/WindowsSelectorImpl") || className.equals("sun/nio/ch/EPollSelectorImpl"))) {
				byteCode = new WindowsSelectorImplTransformer().transform(classfileBuffer);
			}

		} catch (Exception ex) {
			MessageLogger.logErrorWithMessage("Unable to insrument", ex, MainTransformer.class);

		}
		return byteCode;
	}
}
