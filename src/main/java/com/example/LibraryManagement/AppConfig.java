package com.example.LibraryManagement;


import com.example.LibraryManagement.Config.JwtAuthenticationFilter;
import com.example.LibraryManagement.Config.JwtProperties;
import com.example.LibraryManagement.Config.JwtUtil;
import com.example.LibraryManagement.Controllers.AuthController;
import com.example.LibraryManagement.Controllers.BookController;
import com.example.LibraryManagement.Controllers.LoanController;
import com.example.LibraryManagement.Exception.ValidationExceptionHandler;
import com.example.LibraryManagement.Repositories.BookRepository;
import com.example.LibraryManagement.Repositories.LoanRepository;
import com.example.LibraryManagement.Repositories.UserRepository;
import com.example.LibraryManagement.Services.BookService;
import com.example.LibraryManagement.Services.CustomUserDetailsService;
import com.example.LibraryManagement.Services.LoanService;
import com.example.LibraryManagement.Services.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@SpringBootConfiguration
@EnableAutoConfiguration
@EnableConfigurationProperties(JwtProperties.class)
@EnableJpaRepositories(basePackages = "com.example.LibraryManagement.Repositories")
public class AppConfig {

    public static void main(String[] args) {
        SpringApplication.run(AppConfig.class, args);
    }

    // ===== Services =====
    @Bean
    public CustomUserDetailsService customUserDetailsService(UserRepository userRepository) {
        return new CustomUserDetailsService(userRepository);
    }

    @Bean
    public UserService userService(AuthenticationManager authenticationManager,
                                   UserRepository userRepository,
                                   JwtUtil jwtUtil,
                                   PasswordEncoder passwordEncoder,
                                   CustomUserDetailsService userDetailsService) {
        return new UserService(authenticationManager, userRepository, jwtUtil, passwordEncoder, userDetailsService);
    }

    @Bean
    public BookService bookService(BookRepository bookRepository) {
        return new BookService(bookRepository);
    }

    @Bean
    public LoanService loanService(LoanRepository loanRepository,BookService bookService, UserService userService) {
        return new LoanService(loanRepository, bookService, userService);
    }

    // ===== Controllers =====
    @Bean
    public AuthController authController(UserService userService) {
        return new AuthController(userService);
    }

    @Bean
    public BookController bookController(BookService bookService) {
        return new BookController(bookService);
    }

    @Bean
    public LoanController loanController(LoanService loanService) {
        return new LoanController(loanService);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtil jwtUtil,
                                                           CustomUserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtUtil, userDetailsService);
   }

    // ===== JWT =====
    @Bean
    public JwtUtil jwtUtil(JwtProperties jwtProperties) {
        return new JwtUtil(jwtProperties);
    }

    // ===== Security  =====
    @Bean
    public DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService,
                                                            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/books/**").hasAnyRole("ADMIN", "MEMBER")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ===== Custom Exceptions  =====
    @Bean
    public ValidationExceptionHandler customValidationHandler() {
        return new ValidationExceptionHandler();
    }

}
