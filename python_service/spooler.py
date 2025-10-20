import win32print
import pywintypes
import sys
import json

def list_printers():
    printers = [p[2] for p in win32print.EnumPrinters(
        win32print.PRINTER_ENUM_LOCAL | win32print.PRINTER_ENUM_CONNECTIONS)]
    print(json.dumps(printers))

def list_jobs(printer_name):
    try:
        hPrinter = win32print.OpenPrinter(printer_name)
        jobs = win32print.EnumJobs(hPrinter, 0, -1, 1)
        win32print.ClosePrinter(hPrinter)
        jobs_list = [{"jobId": j["JobId"], "documentName": j["pDocument"], "status": j["Status"]} for j in jobs]
        print(json.dumps(jobs_list))
    except pywintypes.error as e:
        print(json.dumps({"error": str(e)}))

def move_job(job_id, new_position, printer_name):
    try:
        hPrinter = win32print.OpenPrinter(printer_name, {"DesiredAccess": win32print.PRINTER_ACCESS_ADMINISTER})
        job_info = win32print.GetJob(hPrinter, int(job_id), 2)
        job_info['Position'] = int(new_position)
        win32print.SetJob(hPrinter, int(job_id), 2, job_info, 0)
        win32print.ClosePrinter(hPrinter)
        sys.exit(0)
    except pywintypes.error as e:
        print(json.dumps({"error": str(e)}))
        sys.exit(1)

def cancel_job(job_id, printer_name):
    try:
        hPrinter = win32print.OpenPrinter(printer_name, {"DesiredAccess": win32print.PRINTER_ACCESS_ADMINISTER})
        win32print.SetJob(hPrinter, int(job_id), 0, None, win32print.JOB_CONTROL_CANCEL)
        win32print.ClosePrinter(hPrinter)
        sys.exit(0)
    except pywintypes.error as e:
        print(json.dumps({"error": str(e)}))
        sys.exit(1)

def pause_printer(printer_name):
    try:
        hPrinter = win32print.OpenPrinter(printer_name, {"DesiredAccess": win32print.PRINTER_ACCESS_ADMINISTER})
        win32print.SetPrinter(hPrinter, 0, None, win32print.PRINTER_CONTROL_PAUSE)
        win32print.ClosePrinter(hPrinter)
        sys.exit(0)
    except pywintypes.error as e:
        print(json.dumps({"error": str(e)}))
        sys.exit(1)

def resume_printer(printer_name):
    try:
        hPrinter = win32print.OpenPrinter(printer_name, {"DesiredAccess": win32print.PRINTER_ACCESS_ADMINISTER})
        win32print.SetPrinter(hPrinter, 0, None, win32print.PRINTER_CONTROL_RESUME)
        win32print.ClosePrinter(hPrinter)
        sys.exit(0)
    except pywintypes.error as e:
        print(json.dumps({"error": str(e)}))
        sys.exit(1)

def pause_job(job_id, printer_name):
    try:
        hPrinter = win32print.OpenPrinter(printer_name, {"DesiredAccess": win32print.PRINTER_ACCESS_ADMINISTER})
        win32print.SetJob(hPrinter, int(job_id), 0, None, win32print.JOB_CONTROL_PAUSE)
        win32print.ClosePrinter(hPrinter)
        sys.exit(0)
    except pywintypes.error as e:
        print(json.dumps({"error": str(e)}))
        sys.exit(1)

def resume_job(job_id, printer_name):
    try:
        hPrinter = win32print.OpenPrinter(printer_name, {"DesiredAccess": win32print.PRINTER_ACCESS_ADMINISTER})
        win32print.SetJob(hPrinter, int(job_id), 0, None, win32print.JOB_CONTROL_RESUME)
        win32print.ClosePrinter(hPrinter)
        sys.exit(0)
    except pywintypes.error as e:
        print(json.dumps({"error": str(e)}))
        sys.exit(1)


if __name__ == "__main__":
    if len(sys.argv) < 2:
        sys.exit(1)

    cmd = sys.argv[1]
    if cmd == "list_printers":
        list_printers()
    elif cmd == "list_jobs" and len(sys.argv) == 3:
        list_jobs(sys.argv[2])
    elif cmd == "move_job" and len(sys.argv) == 5:
        move_job(sys.argv[2], sys.argv[3], sys.argv[4])
    elif cmd == "cancel_job" and len(sys.argv) == 4:
        cancel_job(sys.argv[2], sys.argv[3])
    elif cmd == "pause_printer" and len(sys.argv) == 3:
        pause_printer(sys.argv[2])
    elif cmd == "resume_printer" and len(sys.argv) == 3:
        resume_printer(sys.argv[2])
    elif cmd == "pause_job" and len(sys.argv) == 4:
        pause_job(sys.argv[2], sys.argv[3])
    elif cmd == "resume_job" and len(sys.argv) == 4:
        resume_job(sys.argv[2], sys.argv[3])
    else:
        print(json.dumps({"error": "Invalid command"}))
        sys.exit(1)
