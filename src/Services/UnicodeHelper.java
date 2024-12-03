package Services;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class UnicodeHelper{
    private UnicodeHelper(){}

//İŞLEM YÖNTEMLERİ:
    public static String getCharacterFromUnicodeAfterU(String fourHexBytesAfterU){
        fourHexBytesAfterU = fourHexBytesAfterU.trim();
        int num = Integer.parseInt(fourHexBytesAfterU.trim(), 16);
//        System.err.println("Gelen 16'lık kod :" + fourHexBytesAfterU);
        ByteBuffer byBuf = ByteBuffer.allocate(4);
        byBuf.putInt(num);
        byte[] byts = byBuf.array();
        String value = "";
        try{
            value = new String(byts, "UTF-8");
        }
        catch(UnsupportedEncodingException exc){
            System.out.println("Verilen 'unicode' kodu çözümlenemedi : " + fourHexBytesAfterU);
        }
        return value;
    }
    public static String getCharacterFromUnicode(String unicode){
        String value = null;
        try{
            byte[] byBuf = unicode.getBytes();
            value = new String(byBuf, "UTF-8");
        }
        catch(UnsupportedEncodingException exc){
            System.err.println("Verilen unicode kodu tanınmadı : " + unicode);
        }
        return value;
    }
    public static String getUnicodeFromCharacter(String character){// DÜZENLENECEK
        String value = null;
        Charset chSet = Charset.forName("UTF-8");
        CharsetEncoder chEncoder = chSet.newEncoder();
        try{
            ByteBuffer byts = chEncoder.encode(CharBuffer.wrap(character.toCharArray()));
            value = new String(byts.array(), "UTF-8");
        }
        catch(UnsupportedEncodingException | CharacterCodingException exc){
            System.err.println("Verilen karakter unicode'a çevrilemedi : " + exc.toString());
        }
        return value;
    }
}