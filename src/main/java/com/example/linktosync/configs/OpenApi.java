package com.example.linktosync.configs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;


// this is for api Documentation configure "Swagger"

@OpenAPIDefinition(
    info = @Info(
        contact= @Contact(
            name = "LinktoSync",
            email= "prasanth76200@gmail.com",
            url ="https://linktosync.com"
        ),
        description= "OFFICAL LinktoSync RESTfull API Documentation",
        title = "LinktoSync API Specification",
        // license = "Resticted",
        version = "1.0",
        termsOfService= "Terms Of Service"
    ),
    servers= {
         @Server (
              description= "Local ENV",
              url ="http:localhost:8080"
         ), 
         
         // we want to map the Production envirnoment url
    //      @Server (
    //         description= "PROD ENV",
    //         url ="http:localhost:8080"
    //    )
    }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT auth description",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApi {
    

}

