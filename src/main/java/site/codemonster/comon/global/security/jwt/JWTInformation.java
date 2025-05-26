package site.codemonster.comon.global.security.jwt;

public record JWTInformation (
        String category,
        String uuid,
        String role
)
{
    public static JWTInformation from(String category, String uuid, String role){
        return new JWTInformation(category, uuid, role);
    }
}
