package com.dmt.bankingapp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class EndpointAccess {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        // Config which endpoints will be accessed by lambda expression
        httpSecurity.authorizeHttpRequests(configure -> configure
                // mask:
                .requestMatchers("/","/welcome").permitAll()
                .requestMatchers("/hello").authenticated()
                .requestMatchers("/homepage").authenticated()
                .requestMatchers("/about").permitAll()
                // .requestMatchers(HttpMethod.<REST API METHOD>,<URI as String>).<attribute>
                .requestMatchers(HttpMethod.GET, "/test/**").authenticated()
                .requestMatchers("/logo.png").permitAll()
                //security for RegisterController
                .requestMatchers(HttpMethod.POST, "/register/newClient").anonymous()
                .requestMatchers("/signup").anonymous()
                //security for ClientController
                .requestMatchers(HttpMethod.POST, "/client/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/client/**").authenticated()
                //security for AccountController
                .requestMatchers(HttpMethod.POST, "/account/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/account/**").authenticated()
                //security for TransactionController
                .requestMatchers(HttpMethod.POST, "/transaction/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/transaction/**").authenticated()
                //security for LoanController
                .requestMatchers(HttpMethod.POST, "/loan/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/loan/**").authenticated()
                //security for HistoryController
                .requestMatchers(HttpMethod.GET, "/history/**").authenticated()
                //security for DepositController
                .requestMatchers(HttpMethod.POST, "/deposit/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/deposit/**").authenticated()
                //security for CommissionController
                .requestMatchers(HttpMethod.POST, "/commission/**").authenticated()
                //security for InstallmentController
                .requestMatchers(HttpMethod.GET, "/installment/**").authenticated()
                //security for error
                .requestMatchers("/error/**").permitAll()
                .anyRequest().authenticated()
        )
                //default login form
                .formLogin(form ->
                        form
                                .loginPage("/login")
                                .loginProcessingUrl("/authenticateClient")
                                .permitAll()

                );
        // Set http login as Basic Auth
        httpSecurity.httpBasic(Customizer.withDefaults());

        // Disable cross site request forgery token - more vulnerable
        httpSecurity.csrf(csrf -> csrf.disable());

        return httpSecurity.build();
    }
}
