/***************************************************************************

	fileio.c - file access functions

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package arcadeflex.v078.mame;

import arcadeflex.v078.settings;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

public class fileio
{
    
    public static void downloadFile(String _rom, String _dstDir) {
        String _url_ROM = settings.romUrl+_rom+".zip";
        System.out.println("Downloading "+_url_ROM);
        try (BufferedInputStream inputStream = new BufferedInputStream(new URL(_url_ROM).openStream());
            FileOutputStream fileOS = new FileOutputStream(_dstDir+"/"+_rom+".zip")) {
              byte data[] = new byte[1024];
              int byteContent;
              while ((byteContent = inputStream.read(data, 0, 1024)) != -1) {
                  fileOS.write(data, 0, byteContent);
              }
              fileOS.close();
              
        } catch (IOException e) {
              e.printStackTrace(System.out);
        }
    }
    
/*TODO*///	
/*TODO*///	
/*TODO*///	#ifdef MESS
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///		DEBUGGING
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	/* Verbose outputs to error.log ? */
/*TODO*///	#define VERBOSE 					0
/*TODO*///	
/*TODO*///	/* enable lots of logging */
/*TODO*///	#if VERBOSE
/*TODO*///	#define LOG(x)	logerror x
/*TODO*///	#else
/*TODO*///	#define LOG(x)
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///		CONSTANTS
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	#define PLAIN_FILE				0
/*TODO*///	#define RAM_FILE				1
/*TODO*///	#define ZIPPED_FILE				2
/*TODO*///	#define UNLOADED_ZIPPED_FILE	3
/*TODO*///	
/*TODO*///	#define FILEFLAG_OPENREAD		0x01
/*TODO*///	#define FILEFLAG_OPENWRITE		0x02
/*TODO*///	#define FILEFLAG_HASH			0x04
/*TODO*///	#define FILEFLAG_REVERSE_SEARCH	0x08
/*TODO*///	#define FILEFLAG_VERIFY_ONLY	0x10
/*TODO*///	#define FILEFLAG_NOZIP			0x20
/*TODO*///	
/*TODO*///	#ifdef MESS
/*TODO*///	#define FILEFLAG_ALLOW_ABSOLUTE	0x40
/*TODO*///	#define FILEFLAG_ZIP_PATHS		0x80
/*TODO*///	#define FILEFLAG_CREATE_GAMEDIR	0x100
/*TODO*///	#define FILEFLAG_MUST_EXIST		0x200
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	#ifdef MAME_DEBUG
/*TODO*///	#define DEBUG_COOKIE			0xbaadf00d
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///		TYPE DEFINITIONS
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	struct _mame_file
/*TODO*///	{
/*TODO*///	#ifdef DEBUG_COOKIE
/*TODO*///		UINT32 debug_cookie;
/*TODO*///	#endif
/*TODO*///		osd_file *file;
/*TODO*///		UINT8 *data;
/*TODO*///		UINT64 offset;
/*TODO*///		UINT64 length;
/*TODO*///		UINT8 eof;
/*TODO*///		UINT8 type;
/*TODO*///		char hash[HASH_BUF_SIZE];
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///		PROTOTYPES
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	static mame_file *generic_fopen(int pathtype, const char *gamename, const char *filename, const char* hash, UINT32 flags);
/*TODO*///	static const char *get_extension_for_filetype(int filetype);
/*TODO*///	static int checksum_file(int pathtype, int pathindex, const char *file, UINT8 **p, UINT64 *size, char* hash);
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///		mame_fopen
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	mame_file *mame_fopen(const char *gamename, const char *filename, int filetype, int openforwrite)
/*TODO*///	{
/*TODO*///		/* first verify that we aren't trying to open read-only types as writeables */
/*TODO*///		switch (filetype)
/*TODO*///		{
/*TODO*///			/* read-only cases */
/*TODO*///			case FILETYPE_ROM:
/*TODO*///	#ifndef MESS
/*TODO*///			case FILETYPE_IMAGE:
/*TODO*///	#endif
/*TODO*///			case FILETYPE_SAMPLE:
/*TODO*///			case FILETYPE_HIGHSCORE_DB:
/*TODO*///			case FILETYPE_ARTWORK:
/*TODO*///			case FILETYPE_HISTORY:
/*TODO*///			case FILETYPE_LANGUAGE:
/*TODO*///	#ifndef MESS
/*TODO*///			case FILETYPE_INI:
/*TODO*///	#endif
/*TODO*///				if (openforwrite)
/*TODO*///				{
/*TODO*///					logerror("mame_fopen: type %02x write not supported\n", filetype);
/*TODO*///					return NULL;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///	
/*TODO*///			/* write-only cases */
/*TODO*///			case FILETYPE_SCREENSHOT:
/*TODO*///				if (openforwrite == 0)
/*TODO*///				{
/*TODO*///					logerror("mame_fopen: type %02x read not supported\n", filetype);
/*TODO*///					return NULL;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* now open the file appropriately */
/*TODO*///		switch (filetype)
/*TODO*///		{
/*TODO*///			/* ROM files */
/*TODO*///			case FILETYPE_ROM:
/*TODO*///				return generic_fopen(filetype, gamename, filename, 0, FILEFLAG_OPENREAD | FILEFLAG_HASH);
/*TODO*///	
/*TODO*///			/* read-only disk images */
/*TODO*///			case FILETYPE_IMAGE:
/*TODO*///	#ifndef MESS
/*TODO*///				return generic_fopen(filetype, gamename, filename, 0, FILEFLAG_OPENREAD | FILEFLAG_NOZIP);
/*TODO*///	#else
/*TODO*///				{
/*TODO*///					int flags = FILEFLAG_ALLOW_ABSOLUTE;
/*TODO*///					switch(openforwrite) {
/*TODO*///					case OSD_FOPEN_READ:   
/*TODO*///						flags |= FILEFLAG_OPENREAD | FILEFLAG_ZIP_PATHS;
/*TODO*///						break;   
/*TODO*///					case OSD_FOPEN_WRITE:   
/*TODO*///						flags |= FILEFLAG_OPENWRITE;   
/*TODO*///						break;
/*TODO*///					case OSD_FOPEN_RW:   
/*TODO*///						flags |= FILEFLAG_OPENREAD | FILEFLAG_OPENWRITE | FILEFLAG_MUST_EXIST;   
/*TODO*///						break;   
/*TODO*///					case OSD_FOPEN_RW_CREATE:
/*TODO*///						flags |= FILEFLAG_OPENREAD | FILEFLAG_OPENWRITE;
/*TODO*///						break;
/*TODO*///					} 
/*TODO*///					return generic_fopen(filetype, gamename, filename, 0, flags);
/*TODO*///				}
/*TODO*///	#endif
/*TODO*///	
/*TODO*///			/* differencing disk images */
/*TODO*///			case FILETYPE_IMAGE_DIFF:
/*TODO*///				return generic_fopen(filetype, gamename, filename, 0, FILEFLAG_OPENREAD | FILEFLAG_OPENWRITE);
/*TODO*///	
/*TODO*///			/* samples */
/*TODO*///			case FILETYPE_SAMPLE:
/*TODO*///				return generic_fopen(filetype, gamename, filename, 0, FILEFLAG_OPENREAD);
/*TODO*///	
/*TODO*///			/* artwork files */
/*TODO*///			case FILETYPE_ARTWORK:
/*TODO*///				return generic_fopen(filetype, gamename, filename, 0, FILEFLAG_OPENREAD);
/*TODO*///	
/*TODO*///			/* NVRAM files */
/*TODO*///			case FILETYPE_NVRAM:
/*TODO*///	#ifdef MESS
/*TODO*///				if (filename)
/*TODO*///					return generic_fopen(filetype, gamename, filename, 0, openforwrite ? FILEFLAG_OPENWRITE | FILEFLAG_CREATE_GAMEDIR : FILEFLAG_OPENREAD);
/*TODO*///	#endif
/*TODO*///				return generic_fopen(filetype, NULL, gamename, 0, openforwrite ? FILEFLAG_OPENWRITE : FILEFLAG_OPENREAD);
/*TODO*///	
/*TODO*///			/* high score files */
/*TODO*///			case FILETYPE_HIGHSCORE:
/*TODO*///				if (!mame_highscore_enabled())
/*TODO*///					return NULL;
/*TODO*///				return generic_fopen(filetype, NULL, gamename, 0, openforwrite ? FILEFLAG_OPENWRITE : FILEFLAG_OPENREAD);
/*TODO*///	
/*TODO*///			/* highscore database */
/*TODO*///			case FILETYPE_HIGHSCORE_DB:
/*TODO*///				return generic_fopen(filetype, NULL, filename, 0, FILEFLAG_OPENREAD);
/*TODO*///	
/*TODO*///			/* config files */
/*TODO*///			case FILETYPE_CONFIG:
/*TODO*///				return generic_fopen(filetype, NULL, gamename, 0, openforwrite ? FILEFLAG_OPENWRITE : FILEFLAG_OPENREAD);
/*TODO*///	
/*TODO*///			/* input logs */
/*TODO*///			case FILETYPE_INPUTLOG:
/*TODO*///				return generic_fopen(filetype, NULL, gamename, 0, openforwrite ? FILEFLAG_OPENWRITE : FILEFLAG_OPENREAD);
/*TODO*///	
/*TODO*///			/* save state files */
/*TODO*///			case FILETYPE_STATE:
/*TODO*///	#ifndef MESS
/*TODO*///				return generic_fopen(filetype, NULL, filename, 0, openforwrite ? FILEFLAG_OPENWRITE : FILEFLAG_OPENREAD);
/*TODO*///	#else
/*TODO*///				return generic_fopen(filetype, NULL, filename, 0, FILEFLAG_ALLOW_ABSOLUTE | (openforwrite ? FILEFLAG_OPENWRITE : FILEFLAG_OPENREAD));
/*TODO*///	#endif
/*TODO*///	
/*TODO*///			/* memory card files */
/*TODO*///			case FILETYPE_MEMCARD:
/*TODO*///				return generic_fopen(filetype, NULL, filename, 0, openforwrite ? FILEFLAG_OPENWRITE : FILEFLAG_OPENREAD);
/*TODO*///	
/*TODO*///			/* screenshot files */
/*TODO*///			case FILETYPE_SCREENSHOT:
/*TODO*///				return generic_fopen(filetype, NULL, filename, 0, FILEFLAG_OPENWRITE);
/*TODO*///	
/*TODO*///			/* history files */
/*TODO*///			case FILETYPE_HISTORY:
/*TODO*///				return generic_fopen(filetype, NULL, filename, 0, FILEFLAG_OPENREAD);
/*TODO*///	
/*TODO*///			/* cheat file */
/*TODO*///			case FILETYPE_CHEAT:
/*TODO*///				return generic_fopen(filetype, NULL, filename, 0, FILEFLAG_OPENREAD | (openforwrite ? FILEFLAG_OPENWRITE : 0));
/*TODO*///	
/*TODO*///			/* language file */
/*TODO*///			case FILETYPE_LANGUAGE:
/*TODO*///				return generic_fopen(filetype, NULL, filename, 0, FILEFLAG_OPENREAD);
/*TODO*///	
/*TODO*///			/* ctrlr files */
/*TODO*///			case FILETYPE_CTRLR:
/*TODO*///				return generic_fopen(filetype, gamename, filename, 0, openforwrite ? FILEFLAG_OPENWRITE : FILEFLAG_OPENREAD);
/*TODO*///	
/*TODO*///			/* game specific ini files */
/*TODO*///			case FILETYPE_INI:
/*TODO*///	#ifndef MESS
/*TODO*///				return generic_fopen(filetype, NULL, gamename, 0, FILEFLAG_OPENREAD);
/*TODO*///	#else
/*TODO*///				return generic_fopen(filetype, NULL, gamename, 0, openforwrite ? FILEFLAG_OPENWRITE : FILEFLAG_OPENREAD);
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	#ifdef MESS
/*TODO*///			/* CRC files */
/*TODO*///			case FILETYPE_CRC:
/*TODO*///				return generic_fopen(filetype, NULL, gamename, 0, openforwrite ? FILEFLAG_OPENWRITE : FILEFLAG_OPENREAD);
/*TODO*///	#endif
/*TODO*///	
/*TODO*///			/* anything else */
/*TODO*///			default:
/*TODO*///				logerror("mame_fopen(): unknown filetype %02x\n", filetype);
/*TODO*///				return NULL;
/*TODO*///		}
/*TODO*///		return NULL;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///		mame_fopen_rom
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	/* Similar to mame_fopen(,,FILETYPE_ROM), but lets you specify an expected checksum 
/*TODO*///	   (better encapsulation of the load by CRC used for ZIP files) */
/*TODO*///	mame_file *mame_fopen_rom(const char *gamename, const char *filename, const char* exphash)
/*TODO*///	{
/*TODO*///		return generic_fopen(FILETYPE_ROM, gamename, filename, exphash, FILEFLAG_OPENREAD | FILEFLAG_HASH);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///		mame_fclose
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	void mame_fclose(mame_file *file)
/*TODO*///	{
/*TODO*///	#ifdef DEBUG_COOKIE
/*TODO*///		assert(file->debug_cookie == DEBUG_COOKIE);
/*TODO*///		file->debug_cookie = 0;
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		/* switch off the file type */
/*TODO*///		switch (file->type)
/*TODO*///		{
/*TODO*///			case PLAIN_FILE:
/*TODO*///				osd_fclose(file->file);
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case ZIPPED_FILE:
/*TODO*///			case RAM_FILE:
/*TODO*///				if (file->data)
/*TODO*///					free(file->data);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* free the file data */
/*TODO*///		free(file);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///		mame_faccess
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	int mame_faccess(const char *filename, int filetype)
/*TODO*///	{
/*TODO*///		const char *extension = get_extension_for_filetype(filetype);
/*TODO*///		int pathcount = osd_get_path_count(filetype);
/*TODO*///		char modified_filename[256];
/*TODO*///		int pathindex;
/*TODO*///	
/*TODO*///		/* copy the filename and add an extension */
/*TODO*///		strcpy(modified_filename, filename);
/*TODO*///		if (extension)
/*TODO*///		{
/*TODO*///			char *p = strchr(modified_filename, '.');
/*TODO*///			if (p)
/*TODO*///				strcpy(p, extension);
/*TODO*///			else
/*TODO*///			{
/*TODO*///				strcat(modified_filename, ".");
/*TODO*///				strcat(modified_filename, extension);
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* loop over all paths */
/*TODO*///		for (pathindex = 0; pathindex < pathcount; pathindex++)
/*TODO*///		{
/*TODO*///			char name[256];
/*TODO*///	
/*TODO*///			/* first check the raw filename, in case we're looking for a directory */
/*TODO*///			sprintf(name, "%s", filename);
/*TODO*///			LOG(("mame_faccess: trying %s\n", name));
/*TODO*///			if (osd_get_path_info(filetype, pathindex, name) != PATH_NOT_FOUND)
/*TODO*///				return 1;
/*TODO*///	
/*TODO*///			/* try again with a .zip extension */
/*TODO*///			sprintf(name, "%s.zip", filename);
/*TODO*///			LOG(("mame_faccess: trying %s\n", name));
/*TODO*///			if (osd_get_path_info(filetype, pathindex, name) != PATH_NOT_FOUND)
/*TODO*///				return 1;
/*TODO*///	
/*TODO*///			/* does such a directory (or file) exist? */
/*TODO*///			sprintf(name, "%s", modified_filename);
/*TODO*///			LOG(("mame_faccess: trying %s\n", name));
/*TODO*///			if (osd_get_path_info(filetype, pathindex, name) != PATH_NOT_FOUND)
/*TODO*///				return 1;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* no match */
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///		mame_fread
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	UINT32 mame_fread(mame_file *file, void *buffer, UINT32 length)
/*TODO*///	{
/*TODO*///		/* switch off the file type */
/*TODO*///		switch (file->type)
/*TODO*///		{
/*TODO*///			case PLAIN_FILE:
/*TODO*///				return osd_fread(file->file, buffer, length);
/*TODO*///	
/*TODO*///			case ZIPPED_FILE:
/*TODO*///			case RAM_FILE:
/*TODO*///				if (file->data)
/*TODO*///				{
/*TODO*///					if (file->offset + length > file->length)
/*TODO*///					{
/*TODO*///						length = file->length - file->offset;
/*TODO*///						file->eof = 1;
/*TODO*///					}
/*TODO*///					memcpy(buffer, file->data + file->offset, length);
/*TODO*///					file->offset += length;
/*TODO*///					return length;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///		mame_fwrite
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	UINT32 mame_fwrite(mame_file *file, const void *buffer, UINT32 length)
/*TODO*///	{
/*TODO*///		/* switch off the file type */
/*TODO*///		switch (file->type)
/*TODO*///		{
/*TODO*///			case PLAIN_FILE:
/*TODO*///				return osd_fwrite(file->file, buffer, length);
/*TODO*///		}
/*TODO*///	
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///		mame_fseek
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	int mame_fseek(mame_file *file, INT64 offset, int whence)
/*TODO*///	{
/*TODO*///		int err = 0;
/*TODO*///	
/*TODO*///		/* switch off the file type */
/*TODO*///		switch (file->type)
/*TODO*///		{
/*TODO*///			case PLAIN_FILE:
/*TODO*///				return osd_fseek(file->file, offset, whence);
/*TODO*///	
/*TODO*///			case ZIPPED_FILE:
/*TODO*///			case RAM_FILE:
/*TODO*///				switch (whence)
/*TODO*///				{
/*TODO*///					case SEEK_SET:
/*TODO*///						file->offset = offset;
/*TODO*///						break;
/*TODO*///					case SEEK_CUR:
/*TODO*///						file->offset += offset;
/*TODO*///						break;
/*TODO*///					case SEEK_END:
/*TODO*///						file->offset = file->length + offset;
/*TODO*///						break;
/*TODO*///				}
/*TODO*///				file->eof = 0;
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	
/*TODO*///		return err;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///		mame_fchecksum
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	int mame_fchecksum(const char *gamename, const char *filename, unsigned int *length, char* hash)
/*TODO*///	{
/*TODO*///		mame_file *file;
/*TODO*///	
/*TODO*///		/* first open the file; we pass the source hash because it contains
/*TODO*///		   the expected checksum for the file (used to load by checksum) */
/*TODO*///		file = generic_fopen(FILETYPE_ROM, gamename, filename, hash, FILEFLAG_OPENREAD | FILEFLAG_HASH | FILEFLAG_VERIFY_ONLY);
/*TODO*///	
/*TODO*///		/* if we didn't succeed return -1 */
/*TODO*///		if (file == 0)
/*TODO*///			return -1;
/*TODO*///	
/*TODO*///		/* close the file and save the length & checksum */
/*TODO*///		hash_data_copy(hash, file->hash);
/*TODO*///		*length = file->length;
/*TODO*///		mame_fclose(file);
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///		mame_fsize
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	UINT64 mame_fsize(mame_file *file)
/*TODO*///	{
/*TODO*///		/* switch off the file type */
/*TODO*///		switch (file->type)
/*TODO*///		{
/*TODO*///			case PLAIN_FILE:
/*TODO*///			{
/*TODO*///				int size, offs;
/*TODO*///				offs = osd_ftell(file->file);
/*TODO*///				osd_fseek(file->file, 0, SEEK_END);
/*TODO*///				size = osd_ftell(file->file);
/*TODO*///				osd_fseek(file->file, offs, SEEK_SET);
/*TODO*///				return size;
/*TODO*///			}
/*TODO*///	
/*TODO*///			case RAM_FILE:
/*TODO*///			case ZIPPED_FILE:
/*TODO*///				return file->length;
/*TODO*///		}
/*TODO*///	
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///		mame_fhash
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	const char* mame_fhash(mame_file *file)
/*TODO*///	{
/*TODO*///		return file->hash;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///		mame_fgetc
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	int mame_fgetc(mame_file *file)
/*TODO*///	{
/*TODO*///		unsigned char buffer;
/*TODO*///	
/*TODO*///		/* switch off the file type */
/*TODO*///		switch (file->type)
/*TODO*///		{
/*TODO*///			case PLAIN_FILE:
/*TODO*///				if (osd_fread(file->file, &buffer, 1) == 1)
/*TODO*///					return buffer;
/*TODO*///				return EOF;
/*TODO*///	
/*TODO*///			case RAM_FILE:
/*TODO*///			case ZIPPED_FILE:
/*TODO*///				if (file->offset < file->length)
/*TODO*///					return file->data[file->offset++];
/*TODO*///				else
/*TODO*///					file->eof = 1;
/*TODO*///				return EOF;
/*TODO*///		}
/*TODO*///		return EOF;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///		mame_ungetc
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	int mame_ungetc(int c, mame_file *file)
/*TODO*///	{
/*TODO*///		/* switch off the file type */
/*TODO*///		switch (file->type)
/*TODO*///		{
/*TODO*///			case PLAIN_FILE:
/*TODO*///				if (osd_feof(file->file))
/*TODO*///				{
/*TODO*///					if (osd_fseek(file->file, 0, SEEK_CUR))
/*TODO*///						return c;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					if (osd_fseek(file->file, -1, SEEK_CUR))
/*TODO*///						return c;
/*TODO*///				}
/*TODO*///				return EOF;
/*TODO*///	
/*TODO*///			case RAM_FILE:
/*TODO*///			case ZIPPED_FILE:
/*TODO*///				if (file->eof)
/*TODO*///					file->eof = 0;
/*TODO*///				else if (file->offset > 0)
/*TODO*///				{
/*TODO*///					file->offset--;
/*TODO*///					return c;
/*TODO*///				}
/*TODO*///				return EOF;
/*TODO*///		}
/*TODO*///		return EOF;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///		mame_fgets
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	char *mame_fgets(char *s, int n, mame_file *file)
/*TODO*///	{
/*TODO*///		char *cur = s;
/*TODO*///	
/*TODO*///		/* loop while we have characters */
/*TODO*///		while (n > 0)
/*TODO*///		{
/*TODO*///			int c = mame_fgetc(file);
/*TODO*///			if (c == EOF)
/*TODO*///				break;
/*TODO*///	
/*TODO*///			/* if there's a CR, look for an LF afterwards */
/*TODO*///			if (c == 0x0d)
/*TODO*///			{
/*TODO*///				int c2 = mame_fgetc(file);
/*TODO*///				if (c2 != 0x0a)
/*TODO*///					mame_ungetc(c2, file);
/*TODO*///				*cur++ = 0x0d;
/*TODO*///				n--;
/*TODO*///				break;
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* if there's an LF, reinterp as a CR for consistency */
/*TODO*///			else if (c == 0x0a)
/*TODO*///			{
/*TODO*///				*cur++ = 0x0d;
/*TODO*///				n--;
/*TODO*///				break;
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* otherwise, pop the character in and continue */
/*TODO*///			*cur++ = c;
/*TODO*///			n--;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* if we put nothing in, return NULL */
/*TODO*///		if (cur == s)
/*TODO*///			return NULL;
/*TODO*///	
/*TODO*///		/* otherwise, terminate */
/*TODO*///		if (n > 0)
/*TODO*///			*cur++ = 0;
/*TODO*///		return s;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///		mame_feof
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	int mame_feof(mame_file *file)
/*TODO*///	{
/*TODO*///		/* switch off the file type */
/*TODO*///		switch (file->type)
/*TODO*///		{
/*TODO*///			case PLAIN_FILE:
/*TODO*///				return osd_feof(file->file);
/*TODO*///	
/*TODO*///			case RAM_FILE:
/*TODO*///			case ZIPPED_FILE:
/*TODO*///				return (file->eof);
/*TODO*///		}
/*TODO*///	
/*TODO*///		return 1;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///		mame_ftell
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	UINT64 mame_ftell(mame_file *file)
/*TODO*///	{
/*TODO*///		/* switch off the file type */
/*TODO*///		switch (file->type)
/*TODO*///		{
/*TODO*///			case PLAIN_FILE:
/*TODO*///				return osd_ftell(file->file);
/*TODO*///	
/*TODO*///			case RAM_FILE:
/*TODO*///			case ZIPPED_FILE:
/*TODO*///				return file->offset;
/*TODO*///		}
/*TODO*///	
/*TODO*///		return -1L;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///		mame_fread_swap
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	UINT32 mame_fread_swap(mame_file *file, void *buffer, UINT32 length)
/*TODO*///	{
/*TODO*///		UINT8 *buf;
/*TODO*///		UINT8 temp;
/*TODO*///		int res, i;
/*TODO*///	
/*TODO*///		/* standard read first */
/*TODO*///		res = mame_fread(file, buffer, length);
/*TODO*///	
/*TODO*///		/* swap the result */
/*TODO*///		buf = buffer;
/*TODO*///		for (i = 0; i < res; i += 2)
/*TODO*///		{
/*TODO*///			temp = buf[i];
/*TODO*///			buf[i] = buf[i + 1];
/*TODO*///			buf[i + 1] = temp;
/*TODO*///		}
/*TODO*///	
/*TODO*///		return res;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///		mame_fwrite_swap
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	UINT32 mame_fwrite_swap(mame_file *file, const void *buffer, UINT32 length)
/*TODO*///	{
/*TODO*///		UINT8 *buf;
/*TODO*///		UINT8 temp;
/*TODO*///		int res, i;
/*TODO*///	
/*TODO*///		/* swap the data first */
/*TODO*///		buf = (UINT8 *)buffer;
/*TODO*///		for (i = 0; i < length; i += 2)
/*TODO*///		{
/*TODO*///			temp = buf[i];
/*TODO*///			buf[i] = buf[i + 1];
/*TODO*///			buf[i + 1] = temp;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* do the write */
/*TODO*///		res = mame_fwrite(file, buffer, length);
/*TODO*///	
/*TODO*///		/* swap the data back */
/*TODO*///		for (i = 0; i < length; i += 2)
/*TODO*///		{
/*TODO*///			temp = buf[i];
/*TODO*///			buf[i] = buf[i + 1];
/*TODO*///			buf[i + 1] = temp;
/*TODO*///		}
/*TODO*///	
/*TODO*///		return res;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///		compose_path
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	INLINE void compose_path(char *output, const char *gamename, const char *filename, const char *extension)
/*TODO*///	{
/*TODO*///		char *filename_base = output;
/*TODO*///		*output = 0;
/*TODO*///	
/*TODO*///	#ifdef MESS
/*TODO*///		if (filename && osd_is_absolute_path(filename))
/*TODO*///		{
/*TODO*///			strcpy(output, filename);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		/* if there's a gamename, add that; only add a '/' if there is a filename as well */
/*TODO*///		if (gamename)
/*TODO*///		{
/*TODO*///			strcat(output, gamename);
/*TODO*///			if (filename)
/*TODO*///			{
/*TODO*///				strcat(output, "/");
/*TODO*///				filename_base = &output[strlen(output)];
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* if there's a filename, add that */
/*TODO*///		if (filename)
/*TODO*///			strcat(output, filename);
/*TODO*///	
/*TODO*///		/* if there's no extension in the filename, add the extension */
/*TODO*///		if (extension && !strchr(filename_base, '.'))
/*TODO*///		{
/*TODO*///			strcat(output, ".");
/*TODO*///			strcat(output, extension);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///		get_extension_for_filetype
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	static const char *get_extension_for_filetype(int filetype)
/*TODO*///	{
/*TODO*///		const char *extension;
/*TODO*///	
/*TODO*///		/* now open the file appropriately */
/*TODO*///		switch (filetype)
/*TODO*///		{
/*TODO*///			case FILETYPE_RAW:			/* raw data files */
/*TODO*///			case FILETYPE_ROM:			/* ROM files */
/*TODO*///			case FILETYPE_HIGHSCORE_DB:	/* highscore database/history files */
/*TODO*///			case FILETYPE_HISTORY:		/* game history files */
/*TODO*///			case FILETYPE_CHEAT:		/* cheat file */
/*TODO*///			default:					/* anything else */
/*TODO*///				extension = NULL;
/*TODO*///				break;
/*TODO*///	
/*TODO*///	#ifndef MESS
/*TODO*///			case FILETYPE_IMAGE:		/* disk image files */
/*TODO*///				extension = "chd";
/*TODO*///				break;
/*TODO*///	#endif
/*TODO*///	
/*TODO*///			case FILETYPE_IMAGE_DIFF:	/* differencing drive images */
/*TODO*///				extension = "dif";
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case FILETYPE_SAMPLE:		/* samples */
/*TODO*///				extension = "wav";
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case FILETYPE_ARTWORK:		/* artwork files */
/*TODO*///			case FILETYPE_SCREENSHOT:	/* screenshot files */
/*TODO*///				extension = "png";
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case FILETYPE_NVRAM:		/* NVRAM files */
/*TODO*///				extension = "nv";
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case FILETYPE_HIGHSCORE:	/* high score files */
/*TODO*///				extension = "hi";
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case FILETYPE_LANGUAGE:		/* language files */
/*TODO*///				extension = "lng";
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case FILETYPE_CONFIG:		/* config files */
/*TODO*///				extension = "cfg";
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case FILETYPE_INPUTLOG:		/* input logs */
/*TODO*///				extension = "inp";
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case FILETYPE_STATE:		/* save state files */
/*TODO*///				extension = "sta";
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case FILETYPE_MEMCARD:		/* memory card files */
/*TODO*///				extension = "mem";
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case FILETYPE_CTRLR:		/* config files */
/*TODO*///			case FILETYPE_INI:			/* game specific ini files */
/*TODO*///				extension = "ini";
/*TODO*///				break;
/*TODO*///	
/*TODO*///	#ifdef MESS
/*TODO*///			case FILETYPE_CRC:
/*TODO*///				extension = "crc";
/*TODO*///				break;
/*TODO*///	#endif
/*TODO*///		}
/*TODO*///		return extension;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///		generic_fopen
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	static mame_file *generic_fopen(int pathtype, const char *gamename, const char *filename, const char* hash, UINT32 flags)
/*TODO*///	{
/*TODO*///		static const char *access_modes[] = { "rb", "rb", "wb", "r+b" };
/*TODO*///		const char *extension = get_extension_for_filetype(pathtype);
/*TODO*///		int pathcount = osd_get_path_count(pathtype);
/*TODO*///		int pathindex, pathstart, pathstop, pathinc;
/*TODO*///		mame_file file, *newfile;
/*TODO*///		char tempname[256];
/*TODO*///	
/*TODO*///	#ifdef MESS
/*TODO*///		int is_absolute_path = osd_is_absolute_path(filename);
/*TODO*///		if (is_absolute_path)
/*TODO*///		{
/*TODO*///			if ((flags & FILEFLAG_ALLOW_ABSOLUTE) == 0)
/*TODO*///				return NULL;
/*TODO*///			pathcount = 1;
/*TODO*///		}
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		LOG(("generic_fopen(%d, %s, %s, %s, %X)\n", pathc, gamename, filename, extension, flags));
/*TODO*///	
/*TODO*///		/* reset the file handle */
/*TODO*///		memset(&file, 0, sizeof(file));
/*TODO*///	
/*TODO*///		/* check for incompatible flags */
/*TODO*///		if ((flags & FILEFLAG_OPENWRITE) && (flags & FILEFLAG_HASH))
/*TODO*///			fprintf(stderr, "Can't use HASH option with WRITE option in generic_fopen!\n");
/*TODO*///	
/*TODO*///		/* determine start/stop based on reverse search flag */
/*TODO*///		if (!(flags & FILEFLAG_REVERSE_SEARCH))
/*TODO*///		{
/*TODO*///			pathstart = 0;
/*TODO*///			pathstop = pathcount;
/*TODO*///			pathinc = 1;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			pathstart = pathcount - 1;
/*TODO*///			pathstop = -1;
/*TODO*///			pathinc = -1;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* loop over paths */
/*TODO*///		for (pathindex = pathstart; pathindex != pathstop; pathindex += pathinc)
/*TODO*///		{
/*TODO*///			char name[1024];
/*TODO*///	
/*TODO*///			/* ----------------- STEP 1: OPEN THE FILE RAW -------------------- */
/*TODO*///	
/*TODO*///			/* first look for path/gamename as a directory */
/*TODO*///			compose_path(name, gamename, NULL, NULL);
/*TODO*///			LOG(("Trying %s\n", name));
/*TODO*///	
/*TODO*///	#ifdef MESS
/*TODO*///			if (is_absolute_path)
/*TODO*///			{
/*TODO*///				*name = 0;
/*TODO*///			}
/*TODO*///			else if (flags & FILEFLAG_CREATE_GAMEDIR)
/*TODO*///			{
/*TODO*///				if (osd_get_path_info(pathtype, pathindex, name) == PATH_NOT_FOUND)
/*TODO*///					osd_create_directory(pathtype, pathindex, name);
/*TODO*///			}
/*TODO*///	#endif
/*TODO*///	
/*TODO*///			/* if the directory exists, proceed */
/*TODO*///			if (*name == 0 || osd_get_path_info(pathtype, pathindex, name) == PATH_IS_DIRECTORY)
/*TODO*///			{
/*TODO*///				/* now look for path/gamename/filename.ext */
/*TODO*///				compose_path(name, gamename, filename, extension);
/*TODO*///	
/*TODO*///				/* if we need checksums, load it into RAM and compute it along the way */
/*TODO*///				if (flags & FILEFLAG_HASH)
/*TODO*///				{
/*TODO*///					if (checksum_file(pathtype, pathindex, name, &file.data, &file.length, file.hash) == 0)
/*TODO*///					{
/*TODO*///						file.type = RAM_FILE;
/*TODO*///						break;
/*TODO*///					}
/*TODO*///				}
/*TODO*///	
/*TODO*///	#ifdef MESS
/*TODO*///				else if ((flags & FILEFLAG_MUST_EXIST) && (osd_get_path_info(pathtype, pathindex, name) == PATH_NOT_FOUND))
/*TODO*///				{
/*TODO*///					/* if FILEFLAG_MUST_EXIST is set and the file isn't there, don't open it */
/*TODO*///				}
/*TODO*///	#endif
/*TODO*///	
/*TODO*///				/* otherwise, just open it straight */
/*TODO*///				else
/*TODO*///				{
/*TODO*///					file.type = PLAIN_FILE;
/*TODO*///					file.file = osd_fopen(pathtype, pathindex, name, access_modes[flags & 3]);
/*TODO*///					if (file.file == NULL && (flags & 3) == 3)
/*TODO*///						file.file = osd_fopen(pathtype, pathindex, name, "w+b");
/*TODO*///					if (file.file != NULL)
/*TODO*///						break;
/*TODO*///				}
/*TODO*///	
/*TODO*///	#ifdef MESS
/*TODO*///				if (flags & FILEFLAG_ZIP_PATHS)
/*TODO*///				{
/*TODO*///					int path_info = PATH_NOT_FOUND;
/*TODO*///					const char *oldname = name;
/*TODO*///					const char *zipentryname;
/*TODO*///					char *newname = NULL;
/*TODO*///					char *oldnewname = NULL;
/*TODO*///					char *s;
/*TODO*///					UINT32 ziplength;
/*TODO*///	
/*TODO*///					while ((oldname[0]) && ((path_info = osd_get_path_info(pathtype, pathindex, oldname)) == PATH_NOT_FOUND))
/*TODO*///					{
/*TODO*///						/* get name of parent directory into newname & oldname */
/*TODO*///						newname = osd_dirname(oldname);
/*TODO*///	
/*TODO*///						/* if we are at a "blocking point", break out now */
/*TODO*///						if (newname && !strcmp(oldname, newname))
/*TODO*///							newname = NULL;
/*TODO*///	
/*TODO*///						if (oldnewname)
/*TODO*///							free(oldnewname);
/*TODO*///						oldname = oldnewname = newname;
/*TODO*///						if (newname == 0)
/*TODO*///							break;
/*TODO*///	
/*TODO*///						/* remove any trailing path separator if needed */
/*TODO*///						for (s = newname + strlen(newname) - 1; s >= newname && osd_is_path_separator(*s); s--)
/*TODO*///							*s = '\0';
/*TODO*///					}
/*TODO*///	
/*TODO*///					if (newname)
/*TODO*///					{
/*TODO*///						if ((oldname[0]) &&(path_info == PATH_IS_FILE))
/*TODO*///						{
/*TODO*///							zipentryname = name + strlen(newname);
/*TODO*///							while(osd_is_path_separator(*zipentryname))
/*TODO*///								zipentryname++;
/*TODO*///	
/*TODO*///							if (load_zipped_file(pathtype, pathindex, newname, zipentryname, &file.data, &ziplength) == 0)
/*TODO*///							{
/*TODO*///								unsigned functions;
/*TODO*///								functions = hash_data_used_functions(hash);
/*TODO*///								LOG(("Using (mame_fopen) zip file for %s\n", filename));
/*TODO*///								file.length = ziplength;
/*TODO*///								file.type = ZIPPED_FILE;
/*TODO*///								hash_compute(file.hash, file.data, file.length, functions);
/*TODO*///								break;
/*TODO*///							}
/*TODO*///						}
/*TODO*///						free(newname);
/*TODO*///					}
/*TODO*///				}
/*TODO*///				if (is_absolute_path)
/*TODO*///					continue;
/*TODO*///	#endif
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* ----------------- STEP 2: OPEN THE FILE IN A ZIP -------------------- */
/*TODO*///	
/*TODO*///			/* now look for it within a ZIP file */
/*TODO*///			if (!(flags & (FILEFLAG_OPENWRITE | FILEFLAG_NOZIP)))
/*TODO*///			{
/*TODO*///				/* first look for path/gamename.zip */
/*TODO*///				compose_path(name, gamename, NULL, "zip");
/*TODO*///				LOG(("Trying %s file\n", name));
/*TODO*///	
/*TODO*///				/* if the ZIP file exists, proceed */
/*TODO*///				if (osd_get_path_info(pathtype, pathindex, name) == PATH_IS_FILE)
/*TODO*///				{
/*TODO*///					UINT32 ziplength;
/*TODO*///	
/*TODO*///					/* if the file was able to be extracted from the ZIP, continue */
/*TODO*///					compose_path(tempname, NULL, filename, extension);
/*TODO*///	
/*TODO*///					/* verify-only case */
/*TODO*///					if (flags & FILEFLAG_VERIFY_ONLY)
/*TODO*///					{
/*TODO*///						UINT8 crcs[4];
/*TODO*///						UINT32 crc = 0;
/*TODO*///	
/*TODO*///						/* Since this is a .ZIP file, we extract the CRC from the expected hash
/*TODO*///						   (if any), so that we can load by CRC if needed. We must check that
/*TODO*///						   the hash really contains a CRC, because it could be a NO_DUMP rom
/*TODO*///						   for which we do not know the CRC yet. */
/*TODO*///						if (hash && hash_data_extract_binary_checksum(hash, HASH_CRC, crcs) != 0)
/*TODO*///						{
/*TODO*///							/* Store the CRC in a single DWORD */
/*TODO*///							crc = ((unsigned long)crcs[0] << 24) |
/*TODO*///								  ((unsigned long)crcs[1] << 16) |
/*TODO*///								  ((unsigned long)crcs[2] <<  8) |
/*TODO*///								  ((unsigned long)crcs[3] <<  0);
/*TODO*///						}
/*TODO*///	
/*TODO*///						hash_data_clear(file.hash);
/*TODO*///							
/*TODO*///						if (checksum_zipped_file(pathtype, pathindex, name, tempname, &ziplength, &crc) == 0)
/*TODO*///						{
/*TODO*///							file.length = ziplength;
/*TODO*///							file.type = UNLOADED_ZIPPED_FILE;
/*TODO*///	
/*TODO*///							crcs[0] = (UINT8)(crc >> 24);
/*TODO*///							crcs[1] = (UINT8)(crc >> 16);
/*TODO*///							crcs[2] = (UINT8)(crc >> 8);
/*TODO*///							crcs[3] = (UINT8)(crc >> 0);
/*TODO*///							hash_data_insert_binary_checksum(file.hash, HASH_CRC, crcs);
/*TODO*///							break;
/*TODO*///						}
/*TODO*///					}
/*TODO*///	
/*TODO*///					/* full load case */
/*TODO*///					else
/*TODO*///					{
/*TODO*///						int err;
/*TODO*///	
/*TODO*///						/* Try loading the file */
/*TODO*///						err = load_zipped_file(pathtype, pathindex, name, tempname, &file.data, &ziplength);
/*TODO*///	
/*TODO*///						/* If it failed, since this is a ZIP file, we can try to load by CRC 
/*TODO*///						   if an expected hash has been provided. unzip.c uses this ugly hack 
/*TODO*///						   of specifying the CRC as filename. */
/*TODO*///						if (err && hash)
/*TODO*///						{
/*TODO*///							char crcn[9];
/*TODO*///	
/*TODO*///							hash_data_extract_printable_checksum(hash, HASH_CRC, crcn);
/*TODO*///	
/*TODO*///							err = load_zipped_file(pathtype, pathindex, name, crcn, &file.data, &ziplength);
/*TODO*///						}
/*TODO*///	
/*TODO*///						if (err == 0)
/*TODO*///						{
/*TODO*///							unsigned functions;
/*TODO*///	
/*TODO*///							LOG(("Using (mame_fopen) zip file for %s\n", filename));
/*TODO*///							file.length = ziplength;
/*TODO*///							file.type = ZIPPED_FILE;
/*TODO*///	
/*TODO*///							/* Since we already loaded the file, we can easily calculate the
/*TODO*///							   checksum of all the functions. In practice, we use only the
/*TODO*///							   functions for which we have an expected checksum to compare with. */
/*TODO*///							functions = hash_data_used_functions(hash);
/*TODO*///	
/*TODO*///							/* If user asked for CRC only, and there is an expected checksum
/*TODO*///							   for CRC in the driver, compute only CRC. */
/*TODO*///							if (options.crc_only && (functions & HASH_CRC))
/*TODO*///								functions = HASH_CRC;
/*TODO*///	
/*TODO*///							hash_compute(file.hash, file.data, file.length, functions);
/*TODO*///							break;
/*TODO*///						}
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* if we didn't succeed, just return NULL */
/*TODO*///		if (pathindex == pathstop)
/*TODO*///			return NULL;
/*TODO*///	
/*TODO*///		/* otherwise, duplicate the file */
/*TODO*///		newfile = malloc(sizeof(file));
/*TODO*///		if (newfile)
/*TODO*///		{
/*TODO*///			*newfile = file;
/*TODO*///	#ifdef DEBUG_COOKIE
/*TODO*///			newfile->debug_cookie = DEBUG_COOKIE;
/*TODO*///	#endif
/*TODO*///		}
/*TODO*///	
/*TODO*///		return newfile;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///		checksum_file
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	static int checksum_file(int pathtype, int pathindex, const char *file, UINT8 **p, UINT64 *size, char* hash)
/*TODO*///	{
/*TODO*///		UINT64 length;
/*TODO*///		UINT8 *data;
/*TODO*///		osd_file *f;
/*TODO*///		unsigned int functions;
/*TODO*///	
/*TODO*///		/* open the file */
/*TODO*///		f = osd_fopen(pathtype, pathindex, file, "rb");
/*TODO*///		if (f == 0)
/*TODO*///			return -1;
/*TODO*///	
/*TODO*///		/* determine length of file */
/*TODO*///		if (osd_fseek(f, 0L, SEEK_END) != 0)
/*TODO*///		{
/*TODO*///			osd_fclose(f);
/*TODO*///			return -1;
/*TODO*///		}
/*TODO*///	
/*TODO*///		length = osd_ftell(f);
/*TODO*///		if (length == -1L)
/*TODO*///		{
/*TODO*///			osd_fclose(f);
/*TODO*///			return -1;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* allocate space for entire file */
/*TODO*///		data = malloc(length);
/*TODO*///		if (data == 0)
/*TODO*///		{
/*TODO*///			osd_fclose(f);
/*TODO*///			return -1;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* read entire file into memory */
/*TODO*///		if (osd_fseek(f, 0L, SEEK_SET) != 0)
/*TODO*///		{
/*TODO*///			free(data);
/*TODO*///			osd_fclose(f);
/*TODO*///			return -1;
/*TODO*///		}
/*TODO*///	
/*TODO*///		if (osd_fread(f, data, length) != length)
/*TODO*///		{
/*TODO*///			free(data);
/*TODO*///			osd_fclose(f);
/*TODO*///			return -1;
/*TODO*///		}
/*TODO*///	
/*TODO*///		*size = length;
/*TODO*///	
/*TODO*///		
/*TODO*///		/* compute the checksums (only the functions for which we have an expected
/*TODO*///		   checksum). Take also care of crconly: if the user asked, we will calculate
/*TODO*///		   only the CRC, but only if there is an expected CRC for this file. */
/*TODO*///		functions = hash_data_used_functions(hash);
/*TODO*///		if (options.crc_only && (functions & HASH_CRC))
/*TODO*///			functions = HASH_CRC;
/*TODO*///		hash_compute(hash, data, length, functions);
/*TODO*///	
/*TODO*///		/* if the caller wants the data, give it away, otherwise free it */
/*TODO*///		if (p)
/*TODO*///			*p = data;
/*TODO*///		else
/*TODO*///			free(data);
/*TODO*///	
/*TODO*///		/* close the file */
/*TODO*///		osd_fclose(f);
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///		mame_fputs
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	int mame_fputs(mame_file *f, const char *s)
/*TODO*///	{
/*TODO*///		return mame_fwrite(f, s, strlen(s));
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///		mame_vfprintf
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	int mame_vfprintf(mame_file *f, const char *fmt, va_list va)
/*TODO*///	{
/*TODO*///		char buf[512];
/*TODO*///		vsnprintf(buf, sizeof(buf), fmt, va);
/*TODO*///		return mame_fputs(f, buf);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///		mame_fprintf
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	int CLIB_DECL mame_fprintf(mame_file *f, const char *fmt, ...)
/*TODO*///	{
/*TODO*///		int rc;
/*TODO*///		va_list va;
/*TODO*///		va_start(va, fmt);
/*TODO*///		rc = mame_vfprintf(f, fmt, va);
/*TODO*///		va_end(va);
/*TODO*///		return rc;
/*TODO*///	}
	
}
