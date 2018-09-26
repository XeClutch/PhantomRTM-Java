import java.io.*;
import java.net.*;
import java.nio.*;

public class PhantomRTM
{
	private String int2uint(int i)
	{
		return "0x" + Integer.toHexString(i).toUpperCase();
	}
	private String byteArray2hexStr(byte[] byteArray)
	{
		return javax.xml.bind.DatatypeConverter.printHexBinary(byteArray);
	}
	private byte[] hexStr2ByteArray(String hexStr)
	{
		return javax.xml.bind.DatatypeConverter.parseHexBinary(hexStr);
	}
	
	private String readerstring;
	private String Console;
	private Socket client;
	private BufferedReader sreader;
	private DataOutputStream swriter;
	
	public void Command_Launch(String Path)
	{
		String[] lines = Path.split("\\");
        String Directory = "";
        for (int i = 0; i < lines.length - 1; i++)
            Directory += lines[i] + "\\";
        Command_SendText("magicboot title=" + Path + " directory=" + Directory);
	}
	public void Command_OpenDVDDrive()
	{
		Command_SendText("dvdeject");
	}
	public String Command_SendText(String Text)
	{
		try
		{
			if (Connect(Console, false))
			{
				swriter = new DataOutputStream(client.getOutputStream());
				swriter.writeChars(Text + "\r\n");
				readerstring = sreader.readLine();
				client.close();
				return readerstring;
			}
			return "";
		}
		catch (Exception ex)
		{
			return "";
		}
	}
	public void Command_Shutdown()
	{
		Command_SendText("shutdown");
	}

	public boolean Connect(String XboxIP)
	{
		return Connect(XboxIP, true);
	}
	private boolean Connect(String XboxIP, boolean close)
	{
		try
		{
			client = new Socket();
			client.connect(new InetSocketAddress(XboxIP, 730), 0);
			sreader = new BufferedReader(new InputStreamReader(client.getInputStream()));
			Console = XboxIP;
			readerstring = sreader.readLine().toLowerCase();
			if (close)
				client.close();
			return readerstring == "201- connected";
		}
		catch (Exception ex)
		{
			return false;
		}
	}
	
	public void Debug_Freeze()
    {
        Command_SendText("stop");
    }
    public void Debug_UnFreeze()
    {
        Command_SendText("go");
    }
    
    public void File_DumpMemory(String Path, int Start, int Length)
    {
    	try
    	{
    		FileOutputStream out = new FileOutputStream(Path);
    		out.write(Read_Byte(Start, Length));
    		out.close();
    	}
    	catch (Exception ex)
    	{
    		
    	}
    }
    public void File_SaveMemory(String Path, int Offset)
    {
    	try
    	{
    		FileInputStream in = new FileInputStream(Path);
    		byte[] buffer = new byte[in.available()];
    		in.read(buffer);
    		in.close();
    		Write_Byte(Offset, buffer);
    	}
    	catch (Exception ex)
    	{
    		
    	}
    }
    
    public String Get_ConsoleID()
    {
        return Command_SendText("getconsoleid").replace("200- consoleid=", "");
    }
    public String Get_CPUKey()
    {
        return Command_SendText("getcpukey").replace("200- ", "");
    }
    public String Get_ProcessID()
    {
    	return "0x" + Command_SendText("getpid").replace("200- pid=", "").replace("0x", "").toUpperCase();
    }
    
    public boolean Read_Boolean(int Offset)
    {
    	return Read_Byte(Offset, 1)[0] == 1;
    }
    public byte Read_Byte(int Offset)
    {
    	return Read_Byte(Offset, 1)[0];
    }
    public byte[] Read_Byte(int Offset, int Length)
    {
    	try
    	{
    		if (Connect(Console, false))
    		{
    			swriter = new DataOutputStream(client.getOutputStream());
				swriter.writeChars("getmem addr=" + int2uint(Offset) + " length=" + int2uint(Length) + "\r\n");
				sreader.readLine();
				String resp = sreader.readLine();
				client.close();
				return hexStr2ByteArray(resp);
    		}
    		return new byte[] { (byte)0xDE, (byte)0xAD, (byte)0xDE, (byte)0xAD };
    	}
    	catch (Exception ex)
    	{
    		return new byte[] { (byte)0xDE, (byte)0xAD, (byte)0xDE, (byte)0xAD };
    	}
    }
    public float Read_Float(int Offset)
    {
    	return ByteBuffer.wrap(Read_Byte(Offset, 4)).order(ByteOrder.BIG_ENDIAN).getFloat();
    }
    public short Read_Int16(int Offset)
    {
    	return ByteBuffer.wrap(Read_Byte(Offset, 2)).order(ByteOrder.BIG_ENDIAN).getShort();
    }
    public int Read_Int32(int Offset)
    {
    	return ByteBuffer.wrap(Read_Byte(Offset, 2)).order(ByteOrder.BIG_ENDIAN).getInt();
    }
    public long Read_Int64(int Offset)
    {
    	return ByteBuffer.wrap(Read_Byte(Offset, 2)).order(ByteOrder.BIG_ENDIAN).getLong();
    }
    public String Read_String(int Offset, int Length)
    {
    	String str = "";
    	byte[] buffer = Read_Byte(Offset, Length);
    	for (int i = 0; i < Length; i++)
    		str += buffer.toString();
    	return str;
    }
    
    public void Write_Boolean(int Offset, boolean Data)
    {
    	Write_Byte(Offset, Data ? (byte)1 : (byte)0);
    }
    public void Write_Byte(int Offset, byte Data)
    {
    	Write_Byte(Offset, new byte[] { Data });
    }
    public void Write_Byte(int Offset, byte[] Data)
    {
    	Command_SendText("setmem addr=" + int2uint(Offset) + " data=" + byteArray2hexStr(Data));
    }
    public void Write_Float(int Offset, float Data)
    {
    	Write_Byte(Offset, ByteBuffer.allocate(4).putFloat(Data).array());
    }
    public void Write_Int16(int Offset, short Data)
    {
    	Write_Byte(Offset, ByteBuffer.allocate(2).putShort(Data).array());
    }
    public void Write_Int32(int Offset, int Data)
    {
    	Write_Byte(Offset, ByteBuffer.allocate(4).putInt(Data).array());
    }
    public void Write_Int64(int Offset, long Data)
    {
    	Write_Byte(Offset, ByteBuffer.allocate(8).putLong(Data).array());
    }
    public void Write_NOP(int Offset)
    {
    	Write_Byte(Offset, new byte[] { (byte)0x60, (byte)0x00, (byte)0x00, (byte)0x00 });
    }
    public void Write_String(int Offset, String Data)
    {
    	Write_Byte(Offset, Data.getBytes());
    }
}
