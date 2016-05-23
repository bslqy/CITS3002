# From http://blog.csdn.net/scutshuxue/article/details/6040876



import threading
import sys
import time
import socket
import ssl

AUTHEN ="Something"
class timer(threading.Thread):
    
    def __init__(self):
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.ssl_sock = ssl.wrap_socket(self.sock, ssl_version=ssl.PROTOCOL_SSLv23)
        self.ssl_sock.connect(('localhost',9991))
        self.isrun = True;
        threading.Thread.__init__(self);
	
	
    def send(self,str):
        self.ssl_sock.send(str + "\n")
        
  
		
    def uploadFile(self,filename):
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
		print "No such File or Directory!!!!!!!!!!"	
		
	def changeAUTHEN(self,message):
		global AUTHEN;
		AUTHEN = str(message);
	
	def run(self):
		global AUTHEN;
		while self.isrun:
			receive = self.ssl_sock.recv(1024);
			AUTHEN = receive;
			print("recv ->" +AUTHEN);
        self.sock.close();
        self.ssl_sock.close();
		
		
	def close(self):
		self.isrun == False;
	
    def recvfile(self, filename):
        print "Start download file"
        self.ssl_sock.send("DOWNLOAD"+"sprt"+filename+'\n');
		# If file does not exist then Java server will send back an error message
		
    def authentication(self,username,password):
		global AUTHEN;
		print "Verifing identity"
		self.ssl_sock.send("U&P"+"sprt"+username+"sprt"+password+'\n')
		while (True):
			print AUTHEN
			if(AUTHEN == str("OK\r\n")):
				return AUTHEN;	
			else:	
				print "Please Try again"
				break;	

def main():
    client = timer()
    client.start()
	
	#LOG IN	
    while(True):
		loginMessage = str(raw_input("Please enter username and password as following format: \n username%password \n"));
		username = loginMessage.split("%")[0];
		password = loginMessage.split("%")[1];
		Result = client.authentication(username,password);
		if(Result == str("OK\r\n")):
			print "LOG IN SUCCESSFULLY"
			print "Welcome:\n","Command to be used:\n","-a filename\n" "-c number\n", "-f filename\n","-h hostname:port\n","-n name\n","-u certificate\n","-v filename certificate\n","otherwise input will be treated as normal message"
			break;
    
    while (True):	
	
		receive = client.ssl_sock.recv(1024);
		print("recv ->" +receive);
		
        # get input from user
		message = str(raw_input("send> "));
		
		#Space exists and not occupies the first place  
		if ((message.find(" "))!= -1 and message.find(" ")>0):
		    # Token the message
			splitedMessage = message.split(" ");
			
			if splitedMessage[0] == "-a":
			    # len(splitedMessage) return the size of array after token, need to be exactly 2;
				if len(splitedMessage)==2 and splitedMessage[1]!= "":
				    client.uploadFile(splitedMessage[1]);				
				else:
					print "Usage:\n -a filename\n"			
									
			if splitedMessage[0] == "-c":
				if len(splitedMessage)==2 and splitedMessage[1]!= "":
					print "provide the required circumference (length) of a circle of trust"
				else:		
					print "Usage:\n -c number\n"
				
			if splitedMessage[0] == "-f":
				#-f filename
				if (len(splitedMessage)==2) and splitedMessage[1] !="":
					client.recvfile(splitedMessage[1]);					
				# -f filename -c number or -f filename -n name
				elif len(splitedMessage) == 4 and (splitedMessage[1])!="" and (splitedMessage[2])!="" and (splitedMessage[3])!="":
					if(splitedMessage[2]) == "-c":
						print "-f filename -c number"	
					elif(splitedMessage[2]) == "-n":
						print "-f filename -n name"
				else:	
					print "Usage:\n -f filename\n or -f filename -c number\n or -f filename -n name\n"
				    						
			if splitedMessage[0] == "-h":
				if len(splitedMessage)==2 and splitedMessage[1]!= "":
					client.send()			
				else:
					print "Usage:\n -h hostname:port\n"
					
			if splitedMessage[0] == "-n":
				if len(splitedMessage)==2 and splitedMessage[1]!= "":
					print "require a circle of trust to involve the named person (i.e. their certificate)"			
				else:
					print "Usage:\n -n name\n"
							
			if splitedMessage[0] == "-u":
				if len(splitedMessage)==2 and splitedMessage[1]!= "":
					uploadCertificate(splitedMessage[1]);
					print "upload a certificate to the oldtrusty server"					
				else:
					print "Usage:\n -u certificate\n"
									
			if splitedMessage[0] == "-v":
				#if there are exactly two spaces "-v a b" , normal execution
				if(len(splitedMessage) == 3) and splitedMessage[1] !="" and splitedMessage[2]!="":
					print "vouch for the authenticity of an existing file in the oldtrusty server using the indicated certificate"
				else:
					print "Usage:\n -v filename certificate\n"
														
		elif (message == "-l"):
			client.send("LIST"+"sprt");
			print "list all stored files and how they are protected"
			
		elif(message=="-a") or (message=="-c") or (message=="-f")or (message=="-h") or (message=="-n")or (message=="-u") or (message=="-u") or (message=="-v"):
			print"Usage: \n","-a filename\n","-c number\n", "-f filename\n","-h hostname:port\n","-n name\n","-u certificate\n","-v filename certificate\n"
		
		# exit if the input is 'exit'		
		elif (message == "exit"):
			client.send("EXIT"+"sprt");
			client.close();
			time.sleep(0.01);
			
		#Normal Commmunication , will be deleted afterward. 
		else: 
			print "Other situation"		
			print message;
			client.send("NORMAL"+"sprt"+message);
		
		

if __name__=='__main__':
    main()
    print AUTHEN
