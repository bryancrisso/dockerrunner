import socket

try:
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    print("Socket successfully created")
except socket.error as err:
    print("Socket creation failed with error %s" %(err))

port = 6666

s.connect(("192.168.1.209", port))

print("Successfully connected to host")

s.send("12345\n".encode("utf-8"))
print("Sent data")
data = s.recv(1024).decode()
print(data)

s.close()