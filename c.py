# From http://blog.csdn.net/scutshuxue/article/details/6040876

import threading
import sys
import time
import socket
import ssl

class timer(threading.Thread):
    def __init__(self):
		ConnectionMessage = str(raw_input("Please Enter -h hostname:port to connect a server> "));
		try:
			splitedMsg = ConnectionMessage.split(" ");
			hostname = splitedMsg[1].split(":")[0];
			port = int(splitedMsg[1].split(":")[1]);
			self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
			self.ssl_sock = ssl.wrap_socket(self.sock, ssl_version=ssl.PROTOCOL_SSLv23)
			# self.ssl_sock.connect(('localhost',9991))
			self.ssl_sock.connect((hostname,port))
			self.isrun = True
			threading.Thread.__init__(self);
		except 	RuntimeError:
			print "Please Try again"
		except IndexError:
			print "Please Enter -h hostname:port to connect a server"
	
    def send(self,str):
		self.ssl_sock.send(str + "\n")
 
		
    def run(self): 
        while self.isrun:
            revice =  self.ssl_sock.recv(1024);
            print ("recv> " + revice); 
        self.sock.close();
        self.ssl_sock.close();
        
    def close(self):
		self.isrun=False
	
    def uploadCertificate(self,certificateName):
        print "server ready , now client sending certificate~~"	    
        try:
			if certificateName.split(".")[1] != "crt":
				print "Invalid FileType, Please enter a file with '.crt' extend"
			else:	
				f = open(certificateName,'rb')
				while (True):
					data = f.read();
					#if file is none
					if not data:
						print "CANNOT SEND EMPTY certificate"
						break;
					# This method doesn't not actually send binary data to a the server. Instead, it check the existance of the certificate given a certificateName.	
					# The reason being is that the FileInputStream in java must steam from a File. 	
					self.ssl_sock.send("CER"+"sprt"+certificateName+'\n')
					print "send certificate successfully!"
					break;
				f.close();		
				time.sleep(1)	
        except IOError:
			print "No such file or Directory"
        except IndexError:
		    print "Please enter FileName.File extensions"
	
    def recvfile(self, filename):
        print "Start download file"
        self.ssl_sock.send("DOWNLOAD"+"sprt"+filename+'\n');
		
    def uploadFile(self,filename):
		print "server ready , now client sending file~~"
		try:
			if filename.split(".")[1] == "crt":
				print "Invalid FileType, Please enter a file with '.crt' extend"
			else:
				f = open(filename,'rb')
				while (True):
					data = f.read();
					#if file is none
					if not data:
						print "CANNOT SEND EMPTY FILE"
						break;	
					#sprt stands for seperator
					# send the actually data to the server
					self.ssl_sock.sendall("FILE"+"sprt"+filename+"sprt"+data+'\n')
					print "send File successfully!"
					break;
				f.close();		
				time.sleep(1)
		except IOError:
			print "No such file or Directory"
		except IndexError:
		    print "Please enter FileName.File extensions"	
	
	
    	
		

def main():
    client = timer()
    client.start()
    print "Welcome:\n","Command to be used:\n","Usage: \n","-a filename\n","-c number\n", "-f filename\n","-f filename\n","-f filename -c number\n","-f filename -n name\n","-l -c number\n","-l -n name\n","-u certificate\n","-v filename certificate\n","otherwise input will be treated as normal message"
	
    while (True):	

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
				# -f filename -c number 
				#  -f filename -n name
				elif len(splitedMessage) == 4 and (splitedMessage[1])!="" and (splitedMessage[2])!="" and (splitedMessage[3])!="":
					if(splitedMessage[2]) == "-c":
						send("FC"+'sprt'+splitedMessage[1]+'sprt'+splitedMessage[3])
						print "-f filename -c number"	
					elif(splitedMessage[2]) == "-n":
						send("FN"+'sprt'+splitedMessage[1]+'sprt'+splitedMessage[3])
						print "-f filename -n name"
				else:	
					print "Usage:\n -f filename\n or -f filename -c number\n or -f filename -n name\n"
					
			if splitedMessage[0] == "-n":
				if len(splitedMessage)==2 and splitedMessage[1]!= "":
					print "require a circle of trust to involve the named person (i.e. their certificate)"			
				else:
					print "Usage:\n -n name\n"
							
			if splitedMessage[0] == "-u":
				if len(splitedMessage)==2 and splitedMessage[1]!= "":
					client.uploadCertificate(splitedMessage[1]);
					print "upload a certificate to the oldtrusty server"					
				else:
					print "Usage:\n -u certificate\n"
									
			if splitedMessage[0] == "-v":
				#if there are exactly two spaces "-v filename certificate" , normal execution
				if(len(splitedMessage) == 3) and splitedMessage[1] !="" and splitedMessage[2]!="":
					send("VOUCH"+"sprt"+splitedMessage[1]+"sprt"+splitedMessage[2])
					print "vouch for the authenticity of an existing file in the oldtrusty server using the indicated certificate"
				else:
					print "Usage:\n -v filename certificate\n"
					
			if splitedMessage[0] == "-l":
				if(len(splitedMessage) == 3) and splitedMessage[1] !="" and splitedMessage[2]!="":
					# -l -c number
					if(splitedMessage[1] == "-c"):
						send("LC"+'sprt'+splitedMessage[2]);
					# -l -n name
					elif(plitedMessage[1] == "-n"):
						send("LN"+'sprt'+splitedMessage[2]);
				 														
		elif (message == "-l"):
			client.send("LIST"+"sprt");
			print "list all stored files and how they are protected"
			
		elif(message=="-a") or (message=="-c") or (message=="-f")or (message=="-h") or (message=="-n")or (message=="-u") or (message=="-u") or (message=="-v"):
			print "Usage: \n","-a filename\n","-c number\n", "-f filename\n","-f filename\n","-f filename -c number\n","-f filename -n name\n","-l -c number\n","-l -n name\n","-u certificate\n","-v filename certificate\n"
		
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
