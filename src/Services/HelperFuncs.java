package Services;

import java.util.List;
import java.util.Map;

public class HelperFuncs{

// İŞLEM YÖNTEMLERİ:
    public static <T, U> String printMap(Map<T, U> map){
        StringBuilder buffer = new StringBuilder();
        if(map == null)
            buffer.append("null");
        for(T key : map.keySet()){
            buffer.append(key).append(" : ");
            String sub = "";
            if(map.get(key) instanceof Map){
                buffer.append("{\n");
                buffer.append(printMap((Map)map.get(key)));
                buffer.append("}\n");
            }
            else if(map.get(key) instanceof List){
                    buffer.append("[\n");
                for(Object obj : (List) map.get(key)){
                    if(obj instanceof Map){
                        buffer.append(printMap((Map) obj)).append(",");
                    }
                    else
                        buffer.append(obj);
//                    buffer.append(",");
                    buffer.append("],\n");
                }
                //buffer.deleteCharAt(buffer.lastIndexOf(buffer.toString()));
            }
            else
                buffer.append(map.get(key));
            buffer.append("\n");
        }
        return buffer.toString();
    }
}