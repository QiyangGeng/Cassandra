# https://gitlab.com/crafty-controller/crafty-client
import json
import sys

from crafty_client import CraftyWeb

# Simple script to interface with crafty-client API for the Crafty Controller, a server control platform I am using to
# manage my Minecraft server

# TODO: perhaps change to a timed cache that prints everything

file = open("crafty.json", 'r')
file_parsed = json.loads(file.read())
URL = file_parsed["URL"]
API_TOKEN = file_parsed["API_TOKEN"]
file.close()


# When printing, the output will start with the command name such that it can be found using String.startsWith()
def handle_args(arguments):
    switch = {
        "help": help(),
        "run_command": _run_command(arguments),
        "list_mc_servers": _list_mc_servers(),
        "get_host_stats": _get_host_stats(),
        "start_server": _start_server(arguments),
        "restart_server": _restart_server(arguments),
        "stop_server": _stop_server(arguments),
        "backup_server": _backup_server(arguments),
        "get_server_logs": _get_server_logs(arguments),
        "search_server_logs": _search_server_logs(arguments),
    }
    print(switch.get(arguments[0], ""))


def _help():
    print(''' Available:
    run_command [server_id] [command]
    list_mc_servers
    get_host_stats
    start_server [server_id]
    restart_server [server_id]
    stop_server [server_id]
    backup_server [server_id]
    get_server_logs [server_id]
    search_server_logs [server_id]
    ''')


def _run_command(arguments):
    if len(arguments) < 3:
        print("Insufficient arguments: run_command [server_id] [command]")
        sys.exit(1)
    cweb = CraftyWeb(URL, API_TOKEN)
    print("run_command" + cweb.run_command(arguments[1], arguments[2]))


def _list_mc_servers():
    cweb = CraftyWeb(URL, API_TOKEN)
    print("list_mc_servers" + cweb.list_mc_servers())


def _get_host_stats():
    cweb = CraftyWeb(URL, API_TOKEN)
    print("get_host_stats" + cweb.get_host_stats())


def _start_server(arguments):
    if len(arguments) < 2:
        print("Insufficient arguments: start_server [server_id]")
        sys.exit(1)
    cweb = CraftyWeb(URL, API_TOKEN)
    print("start_server" + cweb.start_server(arguments[1]))


def _restart_server(arguments):
    if len(arguments) < 2:
        print("Insufficient arguments: restart_server [server_id]")
        sys.exit(1)
    cweb = CraftyWeb(URL, API_TOKEN)
    print("restart_server" + cweb.restart_server(arguments[1]))


def _stop_server(arguments):
    if len(arguments) < 2:
        print("Insufficient arguments: stop_server [server_id]")
        sys.exit(1)
    cweb = CraftyWeb(URL, API_TOKEN)
    print("stop_server" + cweb.stop_server(arguments[1]))


def _backup_server(arguments):
    if len(arguments) < 2:
        print("Insufficient arguments: backup_server [server_id]")
        sys.exit(1)
    cweb = CraftyWeb(URL, API_TOKEN)
    print("backup_server" + cweb.backup_server(arguments[1]))


def _get_server_logs(arguments):
    if len(arguments) < 2:
        print("Insufficient arguments: get_server_logs [server_id]")
        sys.exit(1)
    cweb = CraftyWeb(URL, API_TOKEN)
    print("get_server_logs" + cweb.get_server_logs(arguments[1]))


def _search_server_logs(arguments):
    if len(arguments) < 2:
        print("Insufficient arguments: search_server_logs [server_id]")
        sys.exit(1)
    cweb = CraftyWeb(URL, API_TOKEN)
    print("search_server_logs" + cweb.get_server_logs(arguments[1]))


print(sys.version_info)
args = sys.argv
if len(args) < 1:
    print("No arguments found")
    sys.exit(1)

handle_args(args)
