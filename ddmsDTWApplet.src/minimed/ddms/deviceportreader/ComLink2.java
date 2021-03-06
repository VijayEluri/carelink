/*     */ package minimed.ddms.deviceportreader;
/*     */ 
/*     */ import I;
/*     */ import java.io.IOException;
/*     */ import minimed.util.Contract;
/*     */ 
/*     */ public final class ComLink2 extends CommunicationsLinkDefault
/*     */ {
/*     */   private static final int IO_DELAY_MS = 100;
/*     */   private static final byte CMD_TRANSMIT_PACKET = 1;
/*     */   private static final byte CMD_READ_STATUS = 3;
/*     */   private static final byte CMD_READ_PRODUCT_INFO = 4;
/*     */   private static final byte CMD_READ_INTERFACE_STATS = 5;
/*     */   private static final byte CMD_READ_SIGNAL_STRENGTH = 6;
/*     */   private static final byte CMD_READ_DATA = 12;
/*     */   private static final int PRODUCT_INFO_REPLY_SW_VER = 16;
/*     */   private static final int PRODUCT_INFO_REPLY_INTERFACE_DATA_INDEX = 19;
/*     */   private static final byte ACK = 85;
/*     */   private static final byte NAK = 102;
/*     */   private static final int INTERNAL_RESPONSE_CODE = 1;
/*     */   private static final int EXTERNAL_RESPONSE_CODE = 2;
/*     */   private static final int INTERFACE_NUMBER_PARADIGM_RF = 0;
/*     */   private static final int INTERFACE_NUMBER_PARADIGM_USB = 1;
/*     */   private static final int REC_SIZE_MIN = 64;
/*  68 */   private static final String[] COMLINK2_NAK_DESCRIPTIONS_TABLE = { "NO ERROR", "CRC MISMATCH", "COMMAND DATA ERROR", "COMM BUSY AND/OR COMMAND CANNOT BE EXECUTED", "COMMAND NOT SUPPORTED" };
/*     */   private static USBPnPNotifier COMM_DETECTOR;
/*     */   private int m_genericStatus;
/*  81 */   private boolean m_initialized = false;
/*     */   private long m_startTimeMS;
/*     */ 
/*     */   public ComLink2(DeviceListener paramDeviceListener, String paramString)
/*     */   {
/*  93 */     super(paramDeviceListener, paramString, "ComLink2");
/*     */   }
/*     */ 
/*     */   public void execute(DeviceCommand paramDeviceCommand)
/*     */     throws BadDeviceCommException, SerialIOHaltedException
/*     */   {
/* 108 */     Contract.pre(paramDeviceCommand instanceof MM511.Command);
/* 109 */     if (!this.m_initialized) {
/* 110 */       throw new BadDeviceCommException("ComLink2 communications not initialized: command=" + paramDeviceCommand);
/*     */     }
/*     */ 
/* 114 */     USBCommand localUSBCommand = new USBCommand((MM511.Command)paramDeviceCommand, null);
/* 115 */     localUSBCommand.execute();
/*     */   }
/*     */ 
/*     */   public static synchronized boolean isLinkPresent()
/*     */   {
/* 125 */     if (COMM_DETECTOR == null) {
/* 126 */       COMM_DETECTOR = new USBPnPNotifier("CareLinkUSB", false);
/*     */     }
/* 128 */     return COMM_DETECTOR.isDevicePresent();
/*     */   }
/*     */ 
/*     */   public int getSignalStrength()
/*     */     throws BadDeviceCommException, IOException, SerialIOHaltedException
/*     */   {
/* 141 */     int[] arrayOfInt = readSignalStrength();
/*     */ 
/* 143 */     int i = (byte)arrayOfInt[0];
/* 144 */     return i;
/*     */   }
/*     */ 
/*     */   void initCommunicationsIO()
/*     */     throws BadDeviceCommException, IOException, SerialIOHaltedException
/*     */   {
/* 156 */     MedicalDevice.logInfo(this, "initCommunicationsIO: BEGIN...");
/* 157 */     this.m_initialized = false;
/* 158 */     closeUSBPort();
/* 159 */     setCommPort(new USBComm());
/*     */     try
/*     */     {
/* 162 */       getUSBPort().initCommunications();
/*     */     } catch (IOException localIOException) {
/* 164 */       if (!isLinkPresent()) {
/* 165 */         throw new IOException("Link Device not present--reconnect & try again... (" + localIOException + ")");
/*     */       }
/*     */ 
/* 168 */       throw localIOException;
/*     */     }
/*     */ 
/* 172 */     readProductInfo();
/* 173 */     readSignalStrength();
/*     */ 
/* 175 */     this.m_initialized = true;
/* 176 */     MedicalDevice.logInfo(this, "initCommunicationsIO: END...");
/*     */   }
/*     */ 
/*     */   void endCommunicationsIO()
/*     */     throws IOException
/*     */   {
/*     */     try
/*     */     {
/* 187 */       readSignalStrength();
/* 188 */       readInterfaceStatistics();
/*     */     } catch (IOException localIOException) {
/*     */     }
/*     */     catch (BadDeviceCommException localBadDeviceCommException) {
/*     */     }
/* 193 */     closeUSBPort();
/* 194 */     this.m_initialized = false;
/*     */   }
/*     */ 
/*     */   USBComm getUSBPort()
/*     */   {
/* 203 */     return (USBComm)getCommPort();
/*     */   }
/*     */ 
/*     */   private void closeUSBPort()
/*     */   {
/* 210 */     if (getUSBPort() != null) {
/* 211 */       getUSBPort().endCommunications();
/* 212 */       setCommPort(null);
/*     */     }
/*     */   }
/*     */ 
/*     */   private int[] readProductInfo()
/*     */     throws BadDeviceCommException, IOException, SerialIOHaltedException
/*     */   {
/* 641 */     MedicalDevice.logInfoHigh(this, "readProductInfo: obtaining product info...");
/* 642 */     int[] arrayOfInt = sendComLink2Command(4);
/* 643 */     String str1 = arrayOfInt[5] == 1 ? "868.35Mhz" : (arrayOfInt[5] == 0) || (arrayOfInt[5] == 255) ? "916.5Mhz" : "UNKNOWN";
/*     */ 
/* 645 */     String str2 = MedicalDevice.Util.makeString(arrayOfInt, 6, 10);
/*     */ 
/* 647 */     int i = arrayOfInt[18];
/* 648 */     StringBuffer localStringBuffer = new StringBuffer();
/*     */ 
/* 650 */     int j = 19;
/* 651 */     for (; j < 19 + i * 2; j += 2)
/*     */     {
/* 653 */       localStringBuffer.append("Interface" + arrayOfInt[j] + "=");
/* 654 */       int k = arrayOfInt[(j + 1)];
/* 655 */       localStringBuffer.append(k == 3 ? "USB" : k == 1 ? "Paradigm RF" : "UNKNOWN");
/* 656 */       localStringBuffer.append("; ");
/*     */     }
/*     */ 
/* 659 */     MedicalDevice.logInfo(this, "readProductInfo: Serial #=" + arrayOfInt[0] + arrayOfInt[1] + arrayOfInt[2] + ", Product Version=" + arrayOfInt[3] + "." + arrayOfInt[4] + ", RF Freq=" + str1 + ", Product Description=" + str2 + ", Software Version=" + arrayOfInt[16] + "." + arrayOfInt[17] + ", #Interfaces=" + i + ": " + localStringBuffer);
/*     */ 
/* 668 */     return arrayOfInt;
/*     */   }
/*     */ 
/*     */   private int[] readInterfaceStatistics()
/*     */     throws BadDeviceCommException, IOException, SerialIOHaltedException
/*     */   {
/* 681 */     MedicalDevice.logInfoHigh(this, "readInterfaceStatistics: obtaining interface stats...");
/* 682 */     int[] arrayOfInt1 = sendComLink2Command(5, 0);
/*     */ 
/* 684 */     MedicalDevice.logInfoHigh(this, "readInterfaceStatistics:  RF stats = " + decodeInterfaceStatistics(arrayOfInt1));
/*     */ 
/* 687 */     int[] arrayOfInt2 = sendComLink2Command(5, 1);
/*     */ 
/* 689 */     MedicalDevice.logInfoHigh(this, "readInterfaceStatistics: USB stats = " + decodeInterfaceStatistics(arrayOfInt2));
/*     */ 
/* 692 */     return MedicalDevice.Util.concat(arrayOfInt1, arrayOfInt2);
/*     */   }
/*     */ 
/*     */   private String decodeInterfaceStatistics(int[] paramArrayOfInt)
/*     */   {
/* 702 */     int i = paramArrayOfInt[0];
/* 703 */     int j = paramArrayOfInt[1];
/* 704 */     int k = paramArrayOfInt[2];
/* 705 */     int m = paramArrayOfInt[3];
/* 706 */     long l1 = MedicalDevice.Util.makeLong(paramArrayOfInt[4], paramArrayOfInt[5], paramArrayOfInt[6], paramArrayOfInt[7]);
/*     */ 
/* 708 */     long l2 = MedicalDevice.Util.makeLong(paramArrayOfInt[8], paramArrayOfInt[9], paramArrayOfInt[10], paramArrayOfInt[11]);
/*     */ 
/* 711 */     return "CRC Errors=" + i + ", Sequence Errors=" + j + ", NAKS=" + k + ", Timeouts=" + m + ", Packets Received=" + l1 + ", Packets Sent=" + l2;
/*     */   }
/*     */ 
/*     */   private int[] sendComLink2Command(int paramInt)
/*     */     throws IOException, BadDeviceCommException
/*     */   {
/* 729 */     int[] arrayOfInt = new int[2];
/* 730 */     arrayOfInt[0] = paramInt;
/* 731 */     arrayOfInt[1] = 0;
/* 732 */     return sendComLink2Command(arrayOfInt);
/*     */   }
/*     */ 
/*     */   private int[] sendComLink2Command(int paramInt1, int paramInt2)
/*     */     throws IOException, BadDeviceCommException
/*     */   {
/* 747 */     int[] arrayOfInt = new int[3];
/* 748 */     arrayOfInt[0] = paramInt1;
/* 749 */     arrayOfInt[1] = paramInt2;
/* 750 */     arrayOfInt[2] = 0;
/* 751 */     return sendComLink2Command(arrayOfInt);
/*     */   }
/*     */ 
/*     */   private int[] sendComLink2Command(int[] paramArrayOfInt)
/*     */     throws IOException, BadDeviceCommException
/*     */   {
/*     */     try
/*     */     {
/* 764 */       getUSBPort().write(paramArrayOfInt);
/* 765 */       return checkAck();
/*     */     } catch (IOException localIOException) {
/* 767 */       MedicalDevice.logWarning(this, "Caught exception " + localIOException + "; clearing buffers...");
/*     */       try
/*     */       {
/* 770 */         getUSBPort().clearBuffers();
/*     */       } catch (Exception localException) {
/*     */       }
/*     */     }
/* 774 */     throw localIOException;
/*     */   }
/*     */ 
/*     */   private int[] readSignalStrength()
/*     */     throws BadDeviceCommException, IOException, SerialIOHaltedException
/*     */   {
/* 788 */     MedicalDevice.logInfoHigh(this, "readSignalStrength: obtaining signal strength...");
/* 789 */     int[] arrayOfInt = sendComLink2Command(6, 0);
/*     */ 
/* 792 */     int i = (byte)arrayOfInt[0];
/* 793 */     MedicalDevice.logInfo(this, "readSignalStrength: Signal Strength= " + i + " dBm");
/* 794 */     return arrayOfInt;
/*     */   }
/*     */ 
/*     */   private int readStatus()
/*     */     throws BadDeviceCommException, IOException, SerialIOHaltedException
/*     */   {
/* 807 */     MedicalDevice.logInfo(this, "readStatus: obtaining status...");
/* 808 */     int[] arrayOfInt = sendComLink2Command(3);
/*     */ 
/* 811 */     int i = arrayOfInt[0];
/*     */ 
/* 817 */     this.m_genericStatus = arrayOfInt[2];
/*     */ 
/* 820 */     int j = MedicalDevice.Util.makeInt(arrayOfInt[3], arrayOfInt[4]);
/*     */ 
/* 824 */     MedicalDevice.logInfoHigh(this, "readStatus: ComLink2 status follows: commModuleStatus=" + MedicalDevice.Util.getHex(i) + ", genericStatus=" + MedicalDevice.Util.getHex(this.m_genericStatus) + ", rfBytesAvailable=" + j + ", receivedData=" + isStatusReceivedData() + ", RX in progress=" + isStatusReceiveInProgress() + ", TX in progess=" + isStatusTransmitInProgress() + ", Interface Error=" + isStatusInterfaceError() + ", RX overflow=" + isStatusReceiveOverflow() + ", TX overflow=" + isStatusTransmitOverflow());
/*     */ 
/* 836 */     if (i != 0) {
/* 837 */       throw new BadDeviceCommException("readStatus: comm module error is set: " + MedicalDevice.Util.getHex(i));
/*     */     }
/*     */ 
/* 841 */     if (isStatusInterfaceError()) {
/* 842 */       throw new BadDeviceCommException("readStatus: ComLink1 has INTERFACE ERROR");
/*     */     }
/*     */ 
/* 845 */     if (isStatusReceiveOverflow()) {
/* 846 */       throw new BadDeviceCommException("readStatus: ComLink1 has RX OVERFLOW ERROR");
/*     */     }
/*     */ 
/* 849 */     if (isStatusTransmitOverflow()) {
/* 850 */       throw new BadDeviceCommException("readStatus: ComLink1 has TX OVERFLOW ERROR");
/*     */     }
/*     */ 
/* 853 */     return isStatusReceivedData() ? j : 0;
/*     */   }
/*     */ 
/*     */   private boolean isStatusReceivedData()
/*     */   {
/* 862 */     return (this.m_genericStatus & 0x1) > 0;
/*     */   }
/*     */ 
/*     */   private boolean isStatusReceiveInProgress()
/*     */   {
/* 871 */     return (this.m_genericStatus & 0x2) > 0;
/*     */   }
/*     */ 
/*     */   private boolean isStatusTransmitInProgress()
/*     */   {
/* 880 */     return (this.m_genericStatus & 0x4) > 0;
/*     */   }
/*     */ 
/*     */   private boolean isStatusInterfaceError()
/*     */   {
/* 889 */     return (this.m_genericStatus & 0x8) > 0;
/*     */   }
/*     */ 
/*     */   private boolean isStatusReceiveOverflow()
/*     */   {
/* 898 */     return (this.m_genericStatus & 0x10) > 0;
/*     */   }
/*     */ 
/*     */   private boolean isStatusTransmitOverflow()
/*     */   {
/* 907 */     return (this.m_genericStatus & 0x20) > 0;
/*     */   }
/*     */ 
/*     */   private int[] checkAck()
/*     */     throws IOException, BadDeviceCommException
/*     */   {
/* 918 */     MedicalDevice.logInfo(this, "checkAck: retrieving ComLink2 ACK packet...");
/* 919 */     MedicalDevice.Util.sleepMS(100);
/* 920 */     int[] arrayOfInt1 = getUSBPort().read();
/*     */ 
/* 922 */     if (arrayOfInt1.length != 64) {
/* 923 */       throw new BadDeviceCommException("checkAck: incorrect comLinkReply length: " + arrayOfInt1.length);
/*     */     }
/*     */ 
/* 928 */     int i = 0;
/* 929 */     int j = arrayOfInt1[(i++)];
/* 930 */     if (j != 1) {
/* 931 */       throw new BadDeviceCommException("checkAck: bad response code: " + MedicalDevice.Util.getHex(j));
/*     */     }
/*     */ 
/* 936 */     j = arrayOfInt1[(i++)];
/* 937 */     if (j == 102)
/*     */     {
/* 939 */       throw new BadDeviceCommException("checkAck: NAK received; reason code=" + MedicalDevice.Util.getHex(j) + " - " + getComLink2NAKDescription(arrayOfInt1[(i++)]));
/*     */     }
/* 941 */     if (j != 85) {
/* 942 */       throw new BadDeviceCommException("checkAck: bad ACK/NAK value received: " + MedicalDevice.Util.getHex(j));
/*     */     }
/*     */ 
/* 947 */     int[] arrayOfInt2 = new int[arrayOfInt1.length - 3];
/* 948 */     System.arraycopy(arrayOfInt1, 3, arrayOfInt2, 0, arrayOfInt2.length);
/* 949 */     return arrayOfInt2;
/*     */   }
/*     */ 
/*     */   private int calcRecordsRequired(int paramInt)
/*     */   {
/* 959 */     int i = paramInt / 64;
/* 960 */     int j = paramInt % 64;
/* 961 */     int k = i + (j > 0 ? 1 : 0);
/* 962 */     return k;
/*     */   }
/*     */ 
/*     */   private void resetClock()
/*     */   {
/* 969 */     this.m_startTimeMS = System.currentTimeMillis();
/*     */   }
/*     */ 
/*     */   private long getClockMS()
/*     */   {
/* 978 */     return System.currentTimeMillis() - this.m_startTimeMS;
/*     */   }
/*     */ 
/*     */   private String getComLink2NAKDescription(int paramInt)
/*     */   {
/*     */     String str;
/* 990 */     if (paramInt <= COMLINK2_NAK_DESCRIPTIONS_TABLE.length - 1)
/* 991 */       str = COMLINK2_NAK_DESCRIPTIONS_TABLE[paramInt];
/*     */     else {
/* 993 */       str = "UNKNOWN NAK DESCRIPTION";
/*     */     }
/* 995 */     return str;
/*     */   }
/*     */ 
/*     */   private class USBCommand
/*     */   {
/*     */     private static final int NUM_BYTES_AVAIL_WAIT_MS = 100;
/*     */     private static final int NUM_BYTES_AVAIL_TIMEOUT_MS = 1000;
/*     */     private static final int USB_HEADER_LEN = 14;
/*     */     private static final int RESPONSE_TYPE_DEVICE_ACK = 1;
/*     */     private static final int RESPONSE_TYPE_DEVICE_NAK = 2;
/*     */     private static final int RESPONSE_TYPE_DEVICE_DATA_SINGLE = 3;
/*     */     private static final int RESPONSE_TYPE_DEVICE_DATA_MULTIPLE = 4;
/*     */     private static final int RESPONSE_TYPE_TIMEOUT = 5;
/*     */     private static final int TRANSMIT_PACKET_HEADER_LEN = 16;
/*     */     private static final int RF_REPEAT_NONE = 0;
/*     */     private static final int RF_REPEAT_PWR_ON = 85;
/*     */     private static final int END_OF_DATA_BIT = 128;
/*     */     private static final int MAX_DATA_SIZE = 1024;
/*     */     private static final int VENDOR_ID_MEDTRONIC = 1;
/*     */     private static final int DEVICE_TYPE_EXTERNAL = 167;
/*     */     MM511.Command m_deviceCommand;
/*     */     private boolean m_eodSet;
/*     */     private final ComLink2 this$0;
/*     */ 
/*     */     private USBCommand(MM511.Command arg2)
/*     */     {
/* 253 */       this.this$0 = this$1;
/*     */       Object localObject;
/* 254 */       this.m_deviceCommand = localObject;
/*     */     }
/*     */ 
/*     */     public String toString()
/*     */     {
/* 265 */       return this.m_deviceCommand.toString();
/*     */     }
/*     */ 
/*     */     private void execute()
/*     */       throws BadDeviceCommException, SerialIOHaltedException
/*     */     {
/* 278 */       MedicalDevice.m_lastCommandDescription = this.m_deviceCommand.m_description;
/*     */ 
/* 280 */       allocateRawData();
/*     */ 
/* 283 */       sendAndRead();
/*     */     }
/*     */ 
/*     */     private void sendAndRead()
/*     */       throws BadDeviceCommException
/*     */     {
/* 297 */       if (this.this$0.getState() != 7) {
/* 298 */         this.this$0.setState(4);
/*     */       }
/* 300 */       sendDeviceCommand();
/*     */ 
/* 303 */       if ((this.m_deviceCommand.m_rawData.length > 0) && (!this.this$0.getDeviceListener().isHaltRequested())) {
/* 304 */         this.this$0.setState(5);
/* 305 */         this.m_deviceCommand.m_rawData = readDeviceData();
/*     */ 
/* 308 */         this.this$0.notifyDeviceUpdateProgress();
/*     */       }
/*     */ 
/* 312 */       if (this.this$0.getState() == 7)
/* 313 */         this.this$0.setState(4);
/*     */     }
/*     */ 
/*     */     private void sendDeviceCommand()
/*     */       throws BadDeviceCommException, SerialIOHaltedException
/*     */     {
/* 326 */       MedicalDevice.logInfo(this, "sendDeviceCommand: SENDING CMD " + this.m_deviceCommand + "for device #" + this.this$0.getDeviceSerialNumber());
/*     */       try
/*     */       {
/* 330 */         int[] arrayOfInt = buildTransmitPacket();
/*     */ 
/* 332 */         this.this$0.getUSBPort().write(arrayOfInt);
/*     */ 
/* 335 */         MedicalDevice.Util.sleepMS(this.m_deviceCommand.getEffectTime());
/*     */ 
/* 338 */         if ((this.m_deviceCommand.m_commandCode != 93) || (this.m_deviceCommand.m_commandParameters[0] != 0))
/*     */         {
/* 340 */           this.this$0.checkAck();
/*     */         }
/*     */       } catch (IOException localIOException) {
/* 343 */         throw new BadDeviceCommException("sendDeviceCommand: ERROR - an IOException  has occurred processing cmd " + MedicalDevice.Util.getHex(this.m_deviceCommand.m_commandCode) + " (" + this.m_deviceCommand.m_description + "); exception=" + localIOException);
/*     */       }
/*     */     }
/*     */ 
/*     */     private void allocateRawData()
/*     */     {
/* 353 */       this.m_deviceCommand.m_rawData = new int[this.m_deviceCommand.m_bytesPerRecord * this.m_deviceCommand.m_maxRecords];
/*     */     }
/*     */ 
/*     */     private int[] readDeviceData()
/*     */       throws BadDeviceCommException, SerialIOHaltedException
/*     */     {
/* 366 */       int[] arrayOfInt1 = new int[0];
/*     */       try
/*     */       {
/*     */         do
/*     */         {
/* 371 */           int[] arrayOfInt2 = readDeviceDataIO();
/* 372 */           arrayOfInt1 = MedicalDevice.Util.concat(arrayOfInt1, arrayOfInt2);
/*     */ 
/* 374 */           this.this$0.incTotalReadByteCount(arrayOfInt2.length);
/*     */ 
/* 376 */           this.this$0.notifyDeviceUpdateProgress();
/* 377 */           if (!this.m_eodSet) {
/* 378 */             this.this$0.setState(6);
/* 379 */             MedicalDevice.logInfo(this, "readDeviceData: just read " + arrayOfInt2.length + " bytes, total bytes=" + arrayOfInt1.length);
/*     */           }
/*     */         }
/* 382 */         while ((!this.m_eodSet) && (!this.this$0.getDeviceListener().isHaltRequested()));
/*     */ 
/* 384 */         MedicalDevice.logInfo(this, "readDeviceData: read " + arrayOfInt1.length + " bytes");
/*     */       } catch (IOException localIOException) {
/* 386 */         throw new BadDeviceCommException("readDeviceData: ERROR - an IOException  has occurred processing cmd " + MedicalDevice.Util.getHex(this.m_deviceCommand.m_commandCode) + " (" + this.m_deviceCommand.m_description + "); exception=" + localIOException);
/*     */       }
/*     */ 
/* 390 */       return arrayOfInt1;
/*     */     }
/*     */ 
/*     */     private int[] readDeviceDataIO()
/*     */       throws IOException, BadDeviceCommException, SerialIOHaltedException
/*     */     {
/* 409 */       int[] arrayOfInt = readData();
/*     */ 
/* 412 */       this.m_eodSet = ((arrayOfInt[5] & 0x80) > 0);
/*     */ 
/* 415 */       int i = MedicalDevice.Util.makeInt(0x7F & arrayOfInt[5], arrayOfInt[6]);
/*     */ 
/* 418 */       if (i < 64) {
/* 419 */         localObject = "readDeviceDataIO: ERROR - cmd " + this + " resulted in low " + "data byte count of " + i;
/*     */ 
/* 421 */         MedicalDevice.logError(this, (String)localObject);
/* 422 */         throw new BadDeviceCommException((String)localObject);
/*     */       }
/*     */ 
/* 426 */       Object localObject = new int[i];
/* 427 */       System.arraycopy(arrayOfInt, 13, localObject, 0, i);
/*     */ 
/* 430 */       int j = arrayOfInt[(arrayOfInt.length - 1)];
/* 431 */       int k = MedicalDevice.Util.computeCRC8(localObject, 0, localObject.length);
/* 432 */       if (k != j) {
/* 433 */         String str = "readDeviceDataIO: ERROR - cmd " + this + " resulted in bad CRC " + "reply of '" + MedicalDevice.Util.getHexCompact(localObject) + "'; crcCalc=" + MedicalDevice.Util.getHex(k) + " crcDevice=" + MedicalDevice.Util.getHex(j);
/*     */ 
/* 437 */         MedicalDevice.logError(this, str);
/* 438 */         throw new BadDeviceCommException(str);
/*     */       }
/*     */ 
/* 441 */       MedicalDevice.logInfoHigh(this, "readDeviceDataIO: done=" + this.m_eodSet + ", device data[" + localObject.length + "]=<" + MedicalDevice.Util.getHexCompact(localObject) + ">");
/*     */ 
/* 445 */       return (I)localObject;
/*     */     }
/*     */ 
/*     */     private int[] readData()
/*     */       throws BadDeviceCommException, IOException
/*     */     {
/* 459 */       int i = getNumBytesAvailable();
/*     */ 
/* 462 */       int[] arrayOfInt1 = new int[5];
/* 463 */       int j = 0;
/* 464 */       arrayOfInt1[(j++)] = 12;
/* 465 */       arrayOfInt1[(j++)] = 0;
/* 466 */       arrayOfInt1[(j++)] = MedicalDevice.Util.getHighByte(i);
/* 467 */       arrayOfInt1[(j++)] = MedicalDevice.Util.getLowByte(i);
/* 468 */       arrayOfInt1[(j++)] = MedicalDevice.Util.computeCRC8(arrayOfInt1, 0, j - 1);
/*     */ 
/* 471 */       int[] arrayOfInt2 = this.this$0.getUSBPort().writeAndRead(arrayOfInt1, i);
/* 472 */       Contract.invariant(arrayOfInt2.length == i);
/*     */ 
/* 474 */       if (arrayOfInt2.length < 14) {
/* 475 */         throw new BadDeviceCommException("readData: insufficient data: <" + MedicalDevice.Util.getHexCompact(arrayOfInt2) + ">");
/*     */       }
/*     */ 
/* 479 */       MedicalDevice.logInfoHigh(this, "readData: retries=" + arrayOfInt2[3] + ", data[" + arrayOfInt2.length + "]=<" + MedicalDevice.Util.getHexCompact(arrayOfInt2) + ">");
/*     */ 
/* 484 */       j = 0;
/* 485 */       int k = arrayOfInt2[(j++)];
/* 486 */       if (k != 2) {
/* 487 */         throw new BadDeviceCommException("readData: bad response code: " + MedicalDevice.Util.getHex(k));
/*     */       }
/*     */ 
/* 492 */       k = arrayOfInt2[(j++)];
/* 493 */       if (k != 0) {
/* 494 */         throw new BadDeviceCommException("readData: bad interface number: " + MedicalDevice.Util.getHex(k));
/*     */       }
/*     */ 
/* 499 */       k = arrayOfInt2[(j++)];
/* 500 */       if (k == 5) {
/* 501 */         throw new BadDeviceCommException("readData: timeout occurred: " + MedicalDevice.Util.getHex(k));
/*     */       }
/*     */ 
/* 504 */       if (k == 2) {
/* 505 */         throw new BadDeviceCommException("readData: device NAK received: " + MedicalDevice.Util.getHex(k));
/*     */       }
/*     */ 
/* 508 */       if ((k != 1) && (k != 3) && (k != 4))
/*     */       {
/* 510 */         throw new BadDeviceCommException("readData: ERROR - unknown Response Type received: " + MedicalDevice.Util.getHex(k));
/*     */       }
/*     */ 
/* 514 */       return arrayOfInt2;
/*     */     }
/*     */ 
/*     */     private int getNumBytesAvailable()
/*     */       throws BadDeviceCommException, IOException
/*     */     {
/* 525 */       this.this$0.resetClock();
/*     */ 
/* 529 */       int i = this.this$0.readStatus();
/*     */ 
/* 531 */       while ((i == 0) && (this.this$0.getClockMS() < 1000L)) {
/* 532 */         i = this.this$0.readStatus();
/* 533 */         MedicalDevice.Util.sleepMS(100);
/*     */       }
/*     */ 
/* 536 */       return i;
/*     */     }
/*     */ 
/*     */     private int[] buildTransmitPacket()
/*     */     {
/* 548 */       int i = this.m_deviceCommand.m_commandParameterCount;
/*     */ 
/* 552 */       Contract.pre(i <= 1024);
/* 553 */       Contract.pre(this.m_deviceCommand.m_cmdLength > 0);
/*     */ 
/* 555 */       int j = 0;
/* 556 */       int[] arrayOfInt1 = new int[i + 16];
/*     */ 
/* 559 */       arrayOfInt1[(j++)] = 1;
/*     */ 
/* 562 */       arrayOfInt1[(j++)] = 0;
/*     */ 
/* 565 */       arrayOfInt1[(j++)] = 167;
/*     */ 
/* 568 */       arrayOfInt1[(j++)] = 1;
/*     */ 
/* 571 */       int[] arrayOfInt2 = packSerialNumber();
/* 572 */       arrayOfInt1[(j++)] = arrayOfInt2[0];
/* 573 */       arrayOfInt1[(j++)] = arrayOfInt2[1];
/* 574 */       arrayOfInt1[(j++)] = arrayOfInt2[2];
/*     */ 
/* 579 */       arrayOfInt1[(j++)] = (0x80 | MedicalDevice.Util.getHighByte(i));
/* 580 */       arrayOfInt1[(j++)] = MedicalDevice.Util.getLowByte(i);
/*     */ 
/* 583 */       arrayOfInt1[(j++)] = (this.m_deviceCommand.m_commandCode == 93 ? 85 : 0);
/*     */ 
/* 587 */       arrayOfInt1[(j++)] = this.m_deviceCommand.m_maxRetries;
/*     */ 
/* 592 */       int k = this.this$0.calcRecordsRequired(this.m_deviceCommand.m_rawData.length);
/* 593 */       arrayOfInt1[(j++)] = (k > 1 ? 2 : k);
/*     */ 
/* 596 */       arrayOfInt1[(j++)] = 0;
/* 597 */       arrayOfInt1[(j++)] = this.m_deviceCommand.m_commandCode;
/*     */ 
/* 600 */       arrayOfInt1[(j++)] = MedicalDevice.Util.computeCRC8(arrayOfInt1, 0, j - 1);
/*     */ 
/* 603 */       System.arraycopy(this.m_deviceCommand.m_commandParameters, 0, arrayOfInt1, j, i);
/*     */ 
/* 605 */       j += i;
/*     */ 
/* 608 */       arrayOfInt1[(j++)] = MedicalDevice.Util.computeCRC8(this.m_deviceCommand.m_commandParameters, 0, i);
/*     */ 
/* 611 */       this.m_deviceCommand.m_packet = arrayOfInt1;
/* 612 */       return arrayOfInt1;
/*     */     }
/*     */ 
/*     */     private int[] packSerialNumber()
/*     */     {
/* 624 */       Contract.pre(this.this$0.getDeviceSerialNumber() != null);
/* 625 */       Contract.pre(this.this$0.getDeviceSerialNumber().length() == 6);
/*     */ 
/* 627 */       return MedicalDevice.Util.makePackedBCD(this.this$0.getDeviceSerialNumber());
/*     */     }
/*     */ 
/*     */     USBCommand(MM511.Command param1, ComLink2.1 arg3)
/*     */     {
/* 221 */       this(param1);
/*     */     }
/*     */   }
/*     */ }

/* Location:           /home/bewest/Documents/bb/carelink/ddmsDTWApplet.jar
 * Qualified Name:     minimed.ddms.deviceportreader.ComLink2
 * JD-Core Version:    0.6.0
 */