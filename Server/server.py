#!/usr/bin/python

import pyrebase
import os
import multiprocessing

config = {
  "apiKey": "apiKey",
  "authDomain": "projectId.firebaseapp.com",
  "databaseURL": "https://projectId.firebaseio.com/",
  "storageBucket": "projectId.appspot.com"
}

firebase = pyrebase.initialize_app(config)
database = firebase.database()
raspberrypi = firebase.auth().sign_in_with_email_and_password("email", "password") #Login to Firebase

class DetectionProcess(multiprocessing.Process):
    def  __init__(self, ):
        multiprocessing.Process. __init__(self)
        self.exit = multiprocessing.Event()

    def run(self):
        os.system("./start.sh")

    def shutdown(self):
        import time
        time.sleep(5)
        self.exit.set()

if __name__ == "__main__":
    print ("Welcome! Suricata with Raspberry PI!")
    process = DetectionProcess()
    os.system("touch /var/log/suricata/suricata.log")
    os.system("touch /var/log/suricata/fast.log")
    system = open("/var/log/suricata/suricata.log", 'r')
    detection = open("/var/log/suricata/fast.log", 'r')
    database.child("state").set("ready")
    print("Press 'Start' menu in Android.")

    while True:
        try:
            state = database.child("state").get().val()
        except KeyboardInterrupt:
            process.shutdown()
            database.child("state").set("stop")
            os.system("rm -f /var/log/suricata/suricata.log")
            os.system("killall -9 /usr/bin/python")
        else:
            if (state == "start"):
                process.start()
                database.child("state").set("detecting")
            elif (state == "detecting"):
                new_system = system.readline()
                new_detection = detection.readline()

                if (new_system != ""):
                    message = new_system.split(" ")
                    date = message[0]
                    time = message[2]
                    msg = new_system.split(" - ")[1].strip("<").strip(">")
                    priority = "0"
                    content = new_system.split("> - ")[1]
                    data_system = {"date": date, "time": time, "title": msg, "priority": priority, "content": content}

                if (new_detection != ""):
                    message = new_detection.split(" ")
                    datetime = message[0]
                    date = datetime.split("-")[0]
                    time = datetime.split("-")[1].split(".")[0]
                    sid = message[3].split(":")[1]
                    msg = new_detection.split("[**]")[1].split("]")[1].strip()
                    priority = message[message.index("[Priority:") + 1].split("]")[0]
                    protocol = new_detection.split("{")[1].split("}")[0]
                    srcIP = new_detection.split("}")[1].split("-")[0].split(":")[0].strip()
                    dstIP = new_detection.split("}")[1].split(">")[1].split(":")[0].strip()
                    data_detection = {"date": date, "time": time, "sid": sid, "title": msg, "priority": priority, "protocol": protocol, "srcIP": srcIP, "dstIP": dstIP}

                if (new_system != "" and new_detection == ""):
                    database.child("message").child("system").push(data_system, raspberrypi['idToken'])
                elif (new_system == "" and new_detection != ""):
                    database.child("message").child("detection").push(data_detection, raspberrypi['idToken'])
                elif (new_system != "" and new_detection != ""):
                    database.child("message").child("system").push(data_system, raspberrypi['idToken'])
                    database.child("message").child("detection").push(data_detection, raspberrypi['idToken'])

            elif (state == "stop"):
                process.shutdown()
                database.child("state").set("stop")
                os.system("rm -f /var/log/suricata/suricata.log")
                os.system("killall -9 /usr/bin/python")
            elif (state == "shutdown"):
                database.child("state").set("stutdown")
                os.system("shutdown now")
            elif (state == "reboot"):
                database.child("state").set("reboot")
                os.system("reboot now")
