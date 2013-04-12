/********************************************************************\
  * Axel -- A lighter download accelerator for Linux and other Unices. *
 *                                                                    *
 * Copyright 2001 Wilmer van der Gaast                                *
 \********************************************************************/

/* Text interface							*/

/*
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License with
 the Debian GNU/Linux distribution in file /usr/doc/copyright/GPL;
 if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 Suite 330, Boston, MA  02111-1307  USA
 */

#include "axel.h"
static void stop( int signal );
static void print_messages( axel_t *axel );

int run = 1;

#ifdef NOGETOPTLONG
#define getopt_long( a, b, c, d, e ) getopt( a, b, c )
#else
static struct option axel_options[] = {
/* name			has_arg	flag	val */
{ "max-speed", 1, NULL, 's' }, { "num-connections", 1, NULL, 'n' }, { "output",
		1, NULL, 'o' }, { "search", 2, NULL, 'S' },
		{ "no-proxy", 0, NULL, 'N' }, { "quiet", 0, NULL, 'q' }, { "verbose", 0,
				NULL, 'v' }, { "help", 0, NULL, 'h' },
		{ "version", 0, NULL, 'V' }, { "alternate", 0, NULL, 'a' }, { "header",
				1, NULL, 'H' }, { "user-agent", 1, NULL, 'U' }, { NULL, 0, NULL,
				0 } };
#endif

/* For returning string values from functions				*/
static char string[MAX_STRING];

axel_t* gmain(int argc, char *argv[], JNIEnv *env, jobject obj,
void (*setAxelJniInfo)(axel_t* axel, JNIEnv *env, jobject obj)) {
	char fn[MAX_STRING] = "";
	int do_search = 0;
	search_t *search;
	conf_t conf[1];
	axel_t *axel;
	int i, j, cur_head = 0;
	char *s;

#ifdef I18N
	setlocale( LC_ALL, "" );
	bindtextdomain( PACKAGE, LOCALE );
	textdomain( PACKAGE );
#endif

	if (!conf_init(conf)) {
		return (NULL);
	}

	opterr = 0;

	j = -1;
	while (1) {
		int option;

		option = getopt_long(argc, argv, "s:n:o:S::NqvhVaH:U:", axel_options,
				NULL );
		if (option == -1)
			break;

		switch (option) {
		case 'U':
			strncpy(conf->user_agent, optarg, MAX_STRING);
			break;
		case 'H':
			strncpy(conf->add_header[cur_head++], optarg, MAX_STRING);
			break;
		case 's':
			if (!sscanf(optarg, "%i", &conf->max_speed)) {
//				print_help();
				return (NULL);
			}
			break;
		case 'n':
			if (!sscanf(optarg, "%i", &conf->num_connections)) {
//				print_help();
				return (NULL);
			}
			break;
		case 'o':
			strncpy(fn, optarg, MAX_STRING);
			break;
		case 'S':
			do_search = 1;
			if (optarg != NULL )
				if (!sscanf(optarg, "%i", &conf->search_top)) {
//					print_help();
					return (NULL);
				}
			break;
		case 'a':
			conf->alternate_output = 1;
			break;
		case 'N':
			*conf->http_proxy = 0;
			break;
		case 'h':
//			print_help();
			return (NULL);
		case 'v':
			if (j == -1)
				j = 1;
			else
				j++;
			break;
		case 'V':
//			print_version();
			return (NULL);
		case 'q':
			close(1);
			conf->verbose = -1;
			if (open("/dev/null", O_WRONLY) != 1) {
				fprintf(stderr, _("Can't redirect stdout to /dev/null.\n"));
				return (NULL);
			}
			break;
		default:
//			print_help();
			return (NULL);
		}
	}
	conf->add_header_count = cur_head;
	if (j > -1)
		conf->verbose = j;

	if (argc - optind == 0) {
//		print_help();
		return (NULL);
	} else if (strcmp(argv[optind], "-") == 0) {
		s = malloc(MAX_STRING);
		if (scanf("%1024[^\n]s", s) != 1) {
			fprintf(stderr, _("Error when trying to read URL (Too long?).\n"));
			return (NULL);
		}
	} else {
		s = argv[optind];
		if (strlen(s) > MAX_STRING) {
			fprintf(stderr, _("Can't handle URLs of length over %d\n" ),
					MAX_STRING);
			return (NULL);
		}
	}

	printf(_("Initializing download: %s\n"), s);
	if (do_search) {
		search = malloc(sizeof(search_t) * (conf->search_amount + 1));
		memset(search, 0, sizeof(search_t) * (conf->search_amount + 1));
		search[0].conf = conf;
		if (conf->verbose)
			printf(_("Doing search...\n"));
		i = search_makelist(search, s);
		if (i < 0) {
			fprintf(stderr, _("File not found\n" ));
			return (NULL);
		}
		if (conf->verbose)
			printf(_("Testing speeds, this can take a while...\n"));
		j = search_getspeeds(search, i);
		search_sortlist(search, i);
		if (conf->verbose) {
			printf(_("%i usable servers found, will use these URLs:\n"), j);
			j = min( j, conf->search_top );
			printf("%-60s %15s\n", "URL", "Speed");
			for (i = 0; i < j; i++)
				printf("%-70.70s %5i\n", search[i].url, search[i].speed);
			printf("\n");
		}
		axel = axel_new(conf, j, search);
		free(search);
		if (axel->ready == -1) {
			print_messages(axel);
			//axel_close(axel);
			return (axel);
		}
	} else if (argc - optind == 1) {
		axel = axel_new(conf, 0, s);
		if (axel->ready == -1) {
			print_messages(axel);
			//axel_close(axel);
			return (axel);
		}
	} else {
		search = malloc(sizeof(search_t) * (argc - optind));
		memset(search, 0, sizeof(search_t) * (argc - optind));
		for (i = 0; i < (argc - optind); i++)
			strncpy(search[i].url, argv[optind + i], MAX_STRING);
		axel = axel_new(conf, argc - optind, search);
		free(search);
		if (axel->ready == -1) {
			print_messages(axel);
			//axel_close(axel);
			return (axel);
		}
	}
	print_messages(axel);
	if (s != argv[optind]) {
		free(s);
	}

	if (*fn) {
		struct stat buf;

		if (stat(fn, &buf) == 0) {
			if (S_ISDIR( buf.st_mode )) {
				size_t fnlen = strlen(fn);
				size_t axelfnlen = strlen(axel->filename);

				if (fnlen + 1 + axelfnlen + 1 > MAX_STRING) {
					fprintf(stderr, _("Filename too long!\n"));
					axel->errcode=-4;
					return (axel);
				}

				fn[fnlen] = '/';
				memcpy(fn + fnlen + 1, axel->filename, axelfnlen);
				fn[fnlen + 1 + axelfnlen] = '\0';
			}
		}
		sprintf(string, "%s.st", fn);
		if (access(fn, F_OK) == 0)
			if (access(string, F_OK) != 0) {
				fprintf(stderr, _("No state file, cannot resume!\n"));
					axel->errcode=-5;
				return (axel);
			}
		if (access(string, F_OK) == 0)
			if (access(fn, F_OK) != 0) {
				printf(
						_("State file found, but no downloaded data. Starting from scratch.\n" ));
				unlink(string);
			}
		strcpy(axel->filename, fn);
	} else {
		/* Local file existence check					*/
		i = 0;
		s = axel->filename + strlen(axel->filename);
		while (1) {
			sprintf(string, "%s.st", axel->filename);
			if (access(axel->filename, F_OK) == 0) {
				if (axel->conn[0].supported) {
					if (access(string, F_OK) == 0)
						break;
				}
			} else {
				if (access(string, F_OK))
					break;
			}
			sprintf(s, ".%i", i);
			i++;
		}
	}

	if (!axel_open(axel)) {
		print_messages(axel);
		return (axel);
	}
	print_messages(axel);
	axel_start(axel);
	print_messages(axel);

	if (conf->alternate_output) {
		putchar('\n');
	} else {
		if (axel->bytes_done > 0) /* Print first dots if resuming	*/
		{
			putchar('\n');
		}
	}
	axel->start_byte = axel->bytes_done;

	/* Install save_state signal handler for resuming support	*/
	signal(SIGINT, stop);
	signal(SIGTERM, stop);


	//init myaxel
	setAxelJniInfo(axel, env, obj);
	axel->run=1;
	while (!axel->ready && axel->run) {
//		long long int prev, done;
//
//		prev = axel->bytes_done;
		axel_do(axel);
		//axel->progress(axel);
	}
	i = axel->ready ? 0 : 2;

	//axel_close(axel);

	return (axel);
}

/* SIGINT/SIGTERM handler						*/
void stop( int signal )
{
	run = 0;
}


/* Print any message in the axel structure				*/
void print_messages( axel_t *axel )
{
	message_t *m;

	while( axel->message )
	{
		printf( "%s\n", axel->message->text );
		m = axel->message;
		axel->message = axel->message->next;
		free( m );
	}
}
