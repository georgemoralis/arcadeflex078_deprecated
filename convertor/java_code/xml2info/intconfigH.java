#ifndef INTCONFIG_H
#define INTCONFIG_H

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package xml2info;

public class intconfigH
{
	
	#define XML_NS 1
	#define XML_DTD 1
	#define XML_CONTEXT_BYTES 1024
	
	#ifdef USE_LSB
	#define BYTEORDER 1234
	#else
	#define BYTEORDER 4321
	#endif
	
	#define HAVE_MEMMOVE
	
	#define XMLPARSEAPI(type) type
	
	
	#undef XMLPARSEAPI
	
	#endif
}
