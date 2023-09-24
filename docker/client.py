import socket, sys, subprocess

def connect(host, port):
    #connecting to da server
    s.connect((host, port))
    print("Successfully connected to host")

    #sending verification code
    s.send((sys.argv[1]+"\n").encode("utf-8"))

    if not waitForString("session valid"):
        print("Session invalid")
        s.close()
        return False
    
    print("Session validated")

    fileData = receiveFile()
    if fileData[0]:
        print("received tester")
        s.send("received\n".encode("utf-8"))
        f = open(fileData[0], "w")
        f.write(fileData[1])
        f.close()

        fileData = receiveFile()
        if fileData[0]:
            print("received script")
            s.send("received\n".encode("utf-8"))
            f = open(fileData[0], "w")
            f.write(fileData[1])
            f.close()

            output = run()
            print([output])
            sendOutput(output)

    s.close()
        
def run():
    out = subprocess.run(["python3", "tester.py"], stdout=subprocess.PIPE).stdout
    return(str(out, encoding="utf-8"))

def sendOutput(output):
    s.send("completed\n".encode("utf-8"))
    s.send((output+"\n").encode("utf-8"))
    s.send("output transfer complete\n".encode("utf-8"))

def receiveFile():
    message = s.recv(1024).decode().strip()
    fileName = ""
    if message[:7] == "sending":
        fileName = message[8:]
    else:
        return ""
    
    s.send("ready\n".encode("utf-8"))

    fileData = s.recv(1024).decode()

    return (fileName, fileData)

def waitForString(expected):
    result = False
    actual = s.recv(1024).decode().strip()
    if actual == expected:
        result = True
    return result

try:
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    print("Socket successfully created")
except socket.error as err:
    print("Socket creation failed with error %s" %(err))

connect(sys.argv[2], 6666)