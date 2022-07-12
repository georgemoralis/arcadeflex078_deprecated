//============================================================
//
//	win32.c - Win32 main program
//
//============================================================

// standard windows headers
//#define WIN32_LEAN_AND_MEAN
/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package arcadeflex.v078.platform;

import static arcadeflex.v078.mame.mame.*;
import static arcadeflex.v078.platform.config.cli_frontend_init;

public class winmain
{
/*TODO*///	
/*TODO*///	// standard includes
/*TODO*///	
/*TODO*///	// MAME headers
/*TODO*///	
/*TODO*///	// from config.c
/*TODO*///	int  cli_frontend_init (int argc, char **argv);
/*TODO*///	
/*TODO*///	//============================================================
/*TODO*///	//	GLOBAL VARIABLES
/*TODO*///	//============================================================
/*TODO*///	
/*TODO*///	int verbose;
/*TODO*///	
/*TODO*///	// this line prevents globbing on the command line
/*TODO*///	int _CRT_glob = 0;
/*TODO*///	
/*TODO*///	
/*TODO*///	//============================================================
/*TODO*///	//	LOCAL VARIABLES
/*TODO*///	//============================================================
/*TODO*///	
/*TODO*///	static char mapfile_name[MAX_PATH];
/*TODO*///	static LPTOP_LEVEL_EXCEPTION_FILTER pass_thru_filter;
/*TODO*///	
/*TODO*///	static int original_leds;
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	//============================================================
/*TODO*///	//	PROTOTYPES
/*TODO*///	//============================================================
/*TODO*///	
/*TODO*///	static LONG CALLBACK exception_filter(struct _EXCEPTION_POINTERS *info);
/*TODO*///	static const char *lookup_symbol(UINT32 address);
	
	
	
	//============================================================
	//	main
	//============================================================
	
	public static int main(int argc, String[] argv)
	{
		int game_index;
/*TODO*///		char *ext;
		int res = 0;
/*TODO*///	
/*TODO*///		// set up exception handling
/*TODO*///		strcpy(mapfile_name, argv[0]);
/*TODO*///		ext = strchr(mapfile_name, '.');
/*TODO*///		if (ext != 0)
/*TODO*///			strcpy(ext, ".map");
/*TODO*///		else
/*TODO*///			strcat(mapfile_name, ".map");
/*TODO*///		pass_thru_filter = SetUnhandledExceptionFilter(exception_filter);
/*TODO*///	
/*TODO*///		// remember the initial LED states
/*TODO*///		original_leds = osd_get_leds();
	
		// parse config and cmdline options
		game_index = cli_frontend_init (argc, argv);
	
		// have we decided on a game?
		if (game_index != -1)
			res = run_game(game_index);
	
/*TODO*///		// restore the original LED state
/*TODO*///		osd_set_leds(original_leds);
/*TODO*///		exit(res);
            return res;
	}
	
	
	
/*TODO*///	//============================================================
/*TODO*///	//	osd_init
/*TODO*///	//============================================================
/*TODO*///	
/*TODO*///	int osd_init(void)
/*TODO*///	{
/*TODO*///		extern 	int result;
/*TODO*///	
/*TODO*///		result = win32_init_window();
/*TODO*///		if (result == 0)
/*TODO*///			result = win32_init_input();
/*TODO*///		return result;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	//============================================================
/*TODO*///	//	osd_exit
/*TODO*///	//============================================================
/*TODO*///	
/*TODO*///	void osd_exit(void)
/*TODO*///	{
/*TODO*///		extern 	win32_shutdown_input();
/*TODO*///		osd_set_leds(0);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	//============================================================
/*TODO*///	//	exception_filter
/*TODO*///	//============================================================
/*TODO*///	
/*TODO*///	static LONG CALLBACK exception_filter(struct _EXCEPTION_POINTERS *info)
/*TODO*///	{
/*TODO*///		static const struct
/*TODO*///		{
/*TODO*///			DWORD code;
/*TODO*///			const char *string;
/*TODO*///		} exception_table[] =
/*TODO*///		{
/*TODO*///			{ EXCEPTION_ACCESS_VIOLATION,		"ACCESS VIOLATION" },
/*TODO*///			{ EXCEPTION_DATATYPE_MISALIGNMENT,	"DATATYPE MISALIGNMENT" },
/*TODO*///			{ EXCEPTION_BREAKPOINT, 			"BREAKPOINT" },
/*TODO*///			{ EXCEPTION_SINGLE_STEP,			"SINGLE STEP" },
/*TODO*///			{ EXCEPTION_ARRAY_BOUNDS_EXCEEDED,	"ARRAY BOUNDS EXCEEDED" },
/*TODO*///			{ EXCEPTION_FLT_DENORMAL_OPERAND,	"FLOAT DENORMAL OPERAND" },
/*TODO*///			{ EXCEPTION_FLT_DIVIDE_BY_ZERO,		"FLOAT DIVIDE BY ZERO" },
/*TODO*///			{ EXCEPTION_FLT_INEXACT_RESULT,		"FLOAT INEXACT RESULT" },
/*TODO*///			{ EXCEPTION_FLT_INVALID_OPERATION,	"FLOAT INVALID OPERATION" },
/*TODO*///			{ EXCEPTION_FLT_OVERFLOW,			"FLOAT OVERFLOW" },
/*TODO*///			{ EXCEPTION_FLT_STACK_CHECK,		"FLOAT STACK CHECK" },
/*TODO*///			{ EXCEPTION_FLT_UNDERFLOW,			"FLOAT UNDERFLOW" },
/*TODO*///			{ EXCEPTION_INT_DIVIDE_BY_ZERO,		"INTEGER DIVIDE BY ZERO" },
/*TODO*///			{ EXCEPTION_INT_OVERFLOW, 			"INTEGER OVERFLOW" },
/*TODO*///			{ EXCEPTION_PRIV_INSTRUCTION, 		"PRIVILEGED INSTRUCTION" },
/*TODO*///			{ EXCEPTION_IN_PAGE_ERROR, 			"IN PAGE ERROR" },
/*TODO*///			{ EXCEPTION_ILLEGAL_INSTRUCTION, 	"ILLEGAL INSTRUCTION" },
/*TODO*///			{ EXCEPTION_NONCONTINUABLE_EXCEPTION,"NONCONTINUABLE EXCEPTION" },
/*TODO*///			{ EXCEPTION_STACK_OVERFLOW, 		"STACK OVERFLOW" },
/*TODO*///			{ EXCEPTION_INVALID_DISPOSITION, 	"INVALID DISPOSITION" },
/*TODO*///			{ EXCEPTION_GUARD_PAGE, 			"GUARD PAGE VIOLATION" },
/*TODO*///			{ EXCEPTION_INVALID_HANDLE, 		"INVALID HANDLE" },
/*TODO*///			{ 0,								"UNKNOWN EXCEPTION" }
/*TODO*///		};
/*TODO*///		static int already_hit = 0;
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		// if we're hitting this recursively, just exit
/*TODO*///		if (already_hit != 0)
/*TODO*///			return EXCEPTION_EXECUTE_HANDLER;
/*TODO*///		already_hit = 1;
/*TODO*///	
/*TODO*///		// find our man
/*TODO*///		for (i = 0; exception_table[i].code != 0; i++)
/*TODO*///			if (info.ExceptionRecord.ExceptionCode == exception_table[i].code)
/*TODO*///				break;
/*TODO*///	
/*TODO*///		// print the exception type and address
/*TODO*///		fprintf(stderr, "\n-----------------------------------------------------\n");
/*TODO*///		fprintf(stderr, "Exception at EIP=%08X%s: %s\n", (UINT32)info.ExceptionRecord.ExceptionAddress,
/*TODO*///				lookup_symbol((UINT32)info.ExceptionRecord.ExceptionAddress), exception_table[i].string);
/*TODO*///	
/*TODO*///		// for access violations, print more info
/*TODO*///		if (info.ExceptionRecord.ExceptionCode == EXCEPTION_ACCESS_VIOLATION)
/*TODO*///			fprintf(stderr, "While attempting to %s memory at %08X\n",
/*TODO*///					info.ExceptionRecord.ExceptionInformation[0] ? "write" : "read",
/*TODO*///					(UINT32)info.ExceptionRecord.ExceptionInformation[1]);
/*TODO*///	/*
/*TODO*///		UINT32 eip, ebp, esp;
/*TODO*///		// attempt to print a call chain
/*TODO*///		fprintf(stderr, "\nCall chain:\n");
/*TODO*///		eip = (UINT32)info.ExceptionRecord.ExceptionAddress;
/*TODO*///		ebp = info.ContextRecord.Ebp;
/*TODO*///		esp = info.ContextRecord.Esp;
/*TODO*///		while (1)
/*TODO*///		{
/*TODO*///			fprintf(stderr, "\t0x%08x\t%s\n", eip, lookup_symbol(eip));
/*TODO*///	fprintf(stderr, "esp = %08x  ebp = %08x\n", esp, ebp);
/*TODO*///			if (esp - ebp >= 0x10000)
/*TODO*///				break;
/*TODO*///	
/*TODO*///			ebp = *(UINT32 *)ebp;
/*TODO*///			eip = *(UINT32 *)(ebp + 4);
/*TODO*///		}
/*TODO*///	*/
/*TODO*///		// exit
/*TODO*///		return EXCEPTION_EXECUTE_HANDLER;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	//============================================================
/*TODO*///	//	lookup_symbol
/*TODO*///	//============================================================
/*TODO*///	
/*TODO*///	static const char *lookup_symbol(UINT32 address)
/*TODO*///	{
/*TODO*///		static char buffer[1024];
/*TODO*///		FILE *	map = fopen(mapfile_name, "r");
/*TODO*///		char	symbol[1024], best_symbol[1024];
/*TODO*///		UINT32	addr, best_addr = 0;
/*TODO*///		char	line[1024];
/*TODO*///	
/*TODO*///		// if no file, return nothing
/*TODO*///		if (map == NULL)
/*TODO*///			return "";
/*TODO*///	
/*TODO*///		// reset the bests
/*TODO*///		*best_symbol = 0;
/*TODO*///		best_addr = 0;
/*TODO*///	
/*TODO*///		// parse the file, looking for map entries
/*TODO*///		while (fgets(line, sizeof(line) - 1, map))
/*TODO*///			if (!strncmp(line, "                0x", 18))
/*TODO*///				if (sscanf(line, "                0x%08x %s", &addr, symbol) == 2)
/*TODO*///					if (addr <= address && addr > best_addr)
/*TODO*///					{
/*TODO*///						best_addr = addr;
/*TODO*///						strcpy(best_symbol, symbol);
/*TODO*///					}
/*TODO*///	
/*TODO*///		// create the final result
/*TODO*///		if (address - best_addr > 0x10000)
/*TODO*///			return "";
/*TODO*///		sprintf(buffer, " (%s+0x%04x)", best_symbol, address - best_addr);
/*TODO*///		return buffer;
/*TODO*///	}
}

