package com.example.LibraryManagement.Controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification
import spock.mock.DetachedMockFactory

@AutoConfigureMockMvc
@SpringBootTest
abstract class BaseSpec extends Specification {
    protected final static DetachedMockFactory factory = new DetachedMockFactory()
    @Autowired MockMvc mockMvc
}