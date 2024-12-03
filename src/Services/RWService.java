package Services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public class RWService{
    private static RWService rw;
    private String[] acceptableExtensions;
    private boolean isExtNotified = false;// isExtensionNotified, Kabûl edilen uzantı tipleri bildirildiyse 'true' olmalı
    

    private RWService(){}

//İŞLEM YÖNTEMLERİ:
    public void setAcceptableExtensions(String[] acceptableExtensions){
        this.acceptableExtensions = acceptableExtensions;
        isExtNotified = true;
    }
    public boolean canReadable(File file) {
        if(file == null)
            return false;
        FileInputStream  fIStream = null;
        try{
            fIStream = new FileInputStream(file);
        }
        catch(IOException exc){
            //Hatâyı nereye yansıtmalı?
            return false;
        }
        if(fIStream != null)
            return true;
        return false;
    }
    public boolean canReadableForData(File file){// Dosya uzantısını da kontrol ediyor
        boolean keepGoing = false;
        if(isExtNotified){
            String exten = getExtension(file);
            if(exten == null)
                return false;
            for(String s : this.acceptableExtensions){
                if(exten.equalsIgnoreCase(s)){
                    keepGoing = true;
                }
            }
        }
        else
            keepGoing = true;
        if(keepGoing)
            return canReadable(file);
        return false;
    }
    public String getExtension(File file){
        if(!file.isFile())
            return null;
        String[] splitted = file.getPath().split("\\.");
        if(splitted == null)
            return null;
        if(splitted.length < 2)
            return "";
        return splitted[splitted.length - 1].toLowerCase();
    }
    public String readDataAsText(File file){
        if(file == null)
            return null;
        if(!file.isFile())
            return null;
        try{
            FileReader fRead = new FileReader(file);
            BufferedReader buf = new BufferedReader(fRead);
            StringBuilder content = new StringBuilder();
            String line;
            int sayac = 0;
            while((line = buf.readLine()) != null){
                if(sayac != 0)
                    content.append("\n");
                content.append(line);
                sayac++;
            }
            buf.close();
            fRead.close();
            return content.toString();
        }
        catch(IOException exc){
            System.out.println("Hatâ (RWService . readDataAsText) : " + exc.getLocalizedMessage());
        }
        return null;
    }
    public String readDataAsText(String path, String fileName){
        File f = new File(path + "\\" + fileName);
        return readDataAsText(f);
    }
    public String readDataAsText(String filePath){
        if(filePath == null)
            return null;
        if(filePath.isEmpty())
            return null;
        File f = new File(filePath);
        return readDataAsText(f);
    }
    public File produceFile(String fileName, String path){
        File saveLoc = new File(path);
        if(!saveLoc.canWrite())
            return null;
        if(!saveLoc.isDirectory())
            return null;
        File ff = new File(saveLoc.getPath(), fileName);
        return ff;
    }
    public File produceFile(String fileName, String path, boolean correctNameIfNameIsntValid){
        String changedFileName;
        if(correctNameIfNameIsntValid)
            changedFileName = fixFileNameForWindowsOS(fileName);
        else
            changedFileName = fileName;
        System.out.println("changedFileName : " + changedFileName);
        return produceFile(changedFileName, path);
    }
    public boolean produceAndWriteFile(String content, String fileName, String path){
        return writeFile(produceFile(fileName, path), content);
    }
    public boolean produceAndWriteFile(String content, String fileName, String path, boolean correctNameIfNameIsntValid){
        return writeFile(produceFile(fileName, path, correctNameIfNameIsntValid), content);
    }
    public File produceTempFile(String fileNameExtension){
        if(fileNameExtension != null)
            fileNameExtension = "." + fileNameExtension;
        else
            fileNameExtension = "";
        try{
            File work = java.io.File.createTempFile("temp", fileNameExtension);
            return work;
        }
        catch(IOException exc){
            //Hatâyı ele al
            System.out.println("Bir hatâ ile karşılaşıldı (RWService.produceFile) : " + exc.getLocalizedMessage());
            return null;
        }
    }
    public File produceAndWriteTempFile(String fileNameWithExtension, String content){
        File tmp = produceTempFile(fileNameWithExtension);
        boolean isSuccess = writeFile(tmp, content);
        if(!isSuccess){
            //Hatâyı ele al
            return null;
        }
        return tmp;
    }
    public boolean writeFile(File file, String content){
        long space = file.length();
        FileWriter wrt;
        BufferedWriter bufwrt;
        try{
            wrt = new FileWriter(file);//DİSK SEÇİLDİĞİNDE HATÂ VERİYOR
            bufwrt = new BufferedWriter(wrt);
            bufwrt.write(content);
            bufwrt.flush();
            bufwrt.close();
            if(file.length() > space){// Dosya boyutu artmış; yanî veri yazılmış olmalı
                return true;
            }
            else{
                //Hatâyı ele al
                return false;
            }
        }
        catch(IOException ex){
            ex.printStackTrace();
            //Hatâyı ele al
            System.err.println("Veriler dosyaya yazılamadı veyâ dosya boyutu bilgisi alınamadı");
            return false;
        }
    }
    public boolean writeFile(String path, String fileName, String content, boolean isOverride){
        File f = new File(path, fileName);
        boolean append = false;
        if(f != null){
            if(f.exists()){
                if(isOverride){
                    f.delete();
                    try{
                        f.createNewFile();
                    }
                    catch(IOException exc){
                        System.err.println(exc.toString());
                    }
                }
                else
                    append = true;
            }
        }
        if(append){
            // .;.
            return false;
        }
        else
            return writeFile(f, content);
    }
//    public boolean appendFile(File f, String content){
//        
//    }
//    public boolean appendFile(String path, String fileName, String content){
//        
//    }
    public String[] getFileList(String path){
        File file = new File(path);
        if(!file.isDirectory())
            return null;
        return file.list();
    }
    public boolean checkFilePositionForRW(String path){
        File file = new File(path);
        if(file == null)
            return false;
        if(!file.isDirectory())
            return false;
        if(file.canRead() && file.canWrite())
            return true;
        else
            return false;
    }
    public boolean deleteFile(String path, String fileName){
        File file = new File(path + "\\" + fileName);
        if(!file.canWrite())
            return false;
        if(file.isDirectory())
            return false;
        return file.delete();
    }
    public boolean checkFileNameForWindowsOS(String fileName){
        char[] tx = fileName.toCharArray();
        for(char ch : tx){
            if(!checkValidityForWindowsOS(ch))
                return false;
        }
        return true;
    }
    public String fixFileNameForWindowsOS(String fileName){
        return fixFileNameForWindowsOS(fileName, false, '0');
    }
    public String fixFileNameForWindowsOS(String fileName, boolean setNewCharacter, char newCharacter){// Bunun yerine düzenli ifâde yazmalısın
        char[] tx = fileName.toCharArray();
        StringBuilder strNewName = new StringBuilder();
        boolean newCharIsValid = checkValidityForWindowsOS(newCharacter);
        for(char c : tx){
            boolean take = checkValidityForWindowsOS(c);
            if(take)
                strNewName.append(c);
            else{// Karakter geçerli değilse;
                if(setNewCharacter){
                    if(newCharIsValid)
                        strNewName.append(newCharacter);
                }
            }
        }
        if(strNewName.length() == 0)
            strNewName.append(UUID.randomUUID().toString().substring(0, 14));// Rastgele isim oluştur ve ekle
        int dotPoint = strNewName.lastIndexOf(".");
        if(dotPoint != -1 && dotPoint == 0){// Dosya uzantısı varsa ve dosya uzantısından evvel herhangi bir harf yoksa;
            String extWithDot = strNewName.toString();// Dosya uzantısını al;
            strNewName.delete(0, strNewName.length());// StringBuilder'ı temizle
            strNewName.append(UUID.randomUUID().toString().substring(0, 14));// Rastgele isim oluştur ve ekle
            strNewName.append(extWithDot);// Dosya uzantısını geri ekle
        }
        return strNewName.toString();
    }
    // ARKAPLAN İŞLEM YÖNTEMLERİ:
    private boolean checkValidityForWindowsOS(char c){
        boolean canUsable = true;
        switch(c){
                case '?' :{
                    canUsable = false;
                    break;
                }
                case '*' :{
                    canUsable = false;
                    break;
                }
                case '/' :{
                    canUsable = false;
                    break;
                }
                case '\\' :{
                    canUsable = false;
                    break;
                }
                case '{' :{
                    canUsable = false;
                    break;
                }
                case '}' :{
                    canUsable= false;
                    break;
                }
                case '&' :{
                    canUsable = false;
                    break;
                }
                case '%' :{
                    canUsable = false;
                    break;
                }
                case '>' :{
                    canUsable = false;
                    break;
                }
                case '<' :{
                    canUsable = false;
                    break;
                }
                case '@' :{
                    canUsable = false;
                    break;
                }
                case ':' :{
                    canUsable = false;
                    break;
                }
                case '"' :{
                    canUsable = false;
                    break;
                }
            }
        return canUsable;
    }

//ERİŞİM YÖNTEMLERİ:
    //ANA ERİŞİM YÖNTEMİ:
    public static RWService getService(){
        if(rw == null){
            rw = new RWService();
        }
        return rw;
    }
    public String[] getAcceptableExtensions(){
        if(acceptableExtensions == null){
            acceptableExtensions = new String[]{""};
        }
        return acceptableExtensions;
    }
}