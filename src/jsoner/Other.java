package jsoner;

/**
 * Diğer iç sınıflar tarafından ihtiyaç duyulan yardımcı fonksiyon içerir
 * @author Mehmet Akif SOLAK
 */
public class Other{
    protected static ReflectorRuntime.Reflector.CODING_STYLE getCodingStyleForReflector(CODING_STYLE codingStyle){
        if(codingStyle == CODING_STYLE.CAMEL_CASE)
            return ReflectorRuntime.Reflector.CODING_STYLE.CAMEL_CASE;
        else if(codingStyle == CODING_STYLE.SNAKE_CASE)
            return ReflectorRuntime.Reflector.CODING_STYLE.SNAKE_CASE;
        return null;
    }
}