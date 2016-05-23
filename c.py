# From http://blog.csdn.net/scutshuxue/article/details/6040876

import threading
import sys
import time
import socket
import ssl

class timer(threading.Thread):
    def __init__(self):
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.ssl_sock = ssl.wrap_socket(self.sock, ssl_version=ssl.PROTOCOL_SSLv23)
        self.ssl_sock.connect(('localhost',9991))
        self.isrun = True
        threading.Thread.__init__(self);
        
    def run(self): 
        while self.isrun:
            revice =  self.ssl_sock.recv(1024);
            print ("recv> " + revice); 
        self.sock.close();
        self.ssl_sock.close();
        
    def send(self,str):
         self.ssl_sock.send(str + "\n")
        
    def close(self):
		self.isrun=False
		
    def upload(self,filename):
        print "server ready , now client sending file~~"
	try:
		f = open(filename,'rb')
		while (True):
			data = f.read();
			#if file is none
			if not data:
				print "CANNOT SEND EMPTY FILE"
				break;
			#Notify the java server that a file is going to be sent. 		
			#sprt stands for seperator
			self.ssl_sock.sendall("FILE"+"sprt"+filename+"sprt"+data+'\n')
			break;
		f.close();		
		time.sleep(1)
		#Notify the java server that the file is complete
		print "send file success!"	
	except IOError:
		print "No such file or Directory"	
		
    def recvfile(self, filename):
        print "Start download file"
        self.ssl_sock.send("DOWNLOAD"+"sprt"+filename+'\n');
        
	

	  

def main():
    client = timer()
    client.start()
    print "Welcome:\n","Command to be used:\n","-a filename\n" "-c number\n", "-f filename\n","-h hostname:port\n","-n name\n","-u certificate\n","-v filename certificate\n","otherwise input will be treated as normal message"
	
    while (True):
        # get input from user
		
		
		message = str(raw_input("send> "));
		
		#Space exists and not occupies the first place  
		if ((message.find(" "))!= -1 and message.find(" ")>0):
			splitedMessage = message.split(" ");
			if message[0:message.find(" ")] == "-a":
				#if there is a space but  there is nothing following -a "-a "
				#or if there are more than one space following -a "-a  j" or "-a h j"  len(message.split(" ") return the size of array after token, need to be exactly 2;
				if not splitedMessage[1] or len(splitedMessage)>2 :
					print "Usage -a filename\n"					
				#normal execution	
				else:
					client.upload(message[message.find(" ")+1:])			
				
			if message[0:message.find(" ")] == "-c":
				if not (splitedMessage[1]) or len(splitedMessage)>2 :
					print "Usage -c number\n"	
				else:		
					print "provide the required circumference (length) of a circle of trust"
				
			if message[0:message.find(" ")] == "-f":
				if not (splitedMessage[1]) or len(splitedMessage)>4 :
				    print "Usage -f filename\n or -f filename -c number\n or -f filename -n name\n"
				elif len(splitedMessage) == 4 and (splitedMessage[0])!="" and (splitedMessage[1])!="" and (splitedMessage[2])!="" and (splitedMessage[3])!="":
				#-f filename -c number or -f filename -n name
					print "Usage -f filename\n or -f filename -c number\n or -f filename -n name\n"				
				else:	
				    client.recvfile(splitedMessage[1])	
					
			if message[0:message.find(" ")] == "-h":
				if not (message.split(" ")[1]) or len(message.split(" "))>2 :
					print "Usage- h hostname:port\n"					
				else:
					print "provide the remote address hosting the oldtrusty server"
				
				
			if message[0:message.find(" ")] == "-n":
				if not (message.split(" ")[1]) or len(message.split(" "))>2 :
					print "Usage -n name\n"					
				else:
					print "require a circle of trust to involve the named person (i.e. their certificate)"
				
				
			if message[0:message.find(" ")] == "-u":
				if not (message.split(" ")[1]) or len(message.split(" "))>2 :
					print "Usage -u certificate\n"					
				else:
					print "upload a certificate to the oldtrusty server"
				
						
			if message[0:message.find(" ")] == "-v":
				#if there are exactly two spaces "-v a b" , normal execution
				if(len(message.split(" ")) == 3):
					print "vouch for the authenticity of an existing file in the oldtrusty server using the indicated certificate"
				else:
					print "Usage: -v filename certificate\n"
					
					
												
		elif (message == "-l"):
			client.send("LIST"+"sprt");
			print "list all stored files and how they are protected"
			
		
		elif(message=="-a") or (message=="-c") or (message=="-f")or (message=="-h") or (message=="-n")or (message=="-u") or (message=="-u") or (message=="-v"):
			print"Usage :\n","-a filename\n" "-c number\n", "-f filename\n","-h hostname:port\n","-n name\n","-u certificate\n","-v filename certificate\n"
		
		# exit if the input is 'exit'		
		elif (message == "exit"):
			client.send("EXIT"+"sprt");
			client.close();
			time.sleep(0.01);
			
		#Normal Commmunication 	
		else: 
			print "Other situation"		
			print message;
			client.send("NORMAL"+"sprt"+message);
		
		

if __name__=='__main__':
     main()
