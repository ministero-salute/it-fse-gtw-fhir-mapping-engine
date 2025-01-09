package it.finanze.sanita.fse2.gtwfhirmappingenginems.utility;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@Slf4j
class FileUtilityTest {

    @Test
    @DisplayName("Test private constructor via reflection")
    void testPrivateConstructor() throws Exception {
        Constructor<FileUtility> constructor = FileUtility.class.getDeclaredConstructor();
        assertFalse(constructor.canAccess(null), "Constructor should be private");
        constructor.setAccessible(true);
        FileUtility instance = constructor.newInstance();
        assertNotNull(instance, "Should instantiate the private constructor for coverage");
    }


    @Test
    @DisplayName("Test getFileFromInternalResources() - normal flow")
    void testGetFileFromInternalResources_normal() {
        byte[] data = FileUtility.getFileFromInternalResources("test_resource.txt");
        assertNotNull(data, "Expected to find and read the resource file");
        assertTrue(data.length > 0, "Expected some bytes from the resource file");
    }

    @Test
    @DisplayName("Test getFileFromInternalResources() - resource not found => returns null")
    void testGetFileFromInternalResources_notFound() {
        // This resource presumably doesn't exist
        byte[] data = FileUtility.getFileFromInternalResources("non_existent.test");
        assertNull(data, "Expected null if resource is not found or an exception occurred");
    }

    @Test
    @DisplayName("Test getByteFromInputStream() - normal flow (reflection)")
    void testGetByteFromInputStream_normal() throws Exception {
        String text = "Test";
        InputStream is = new ByteArrayInputStream(text.getBytes());

        byte[] result = invokeGetByteFromInputStream(is);

        assertNotNull(result);
        assertEquals(text, new String(result), "Content should match the input data");
    }

    @Test
    @DisplayName("Test getByteFromInputStream() - exception flow => BusinessException")
    void testGetByteFromInputStream_exception() throws Exception {
        // Mock an InputStream that throws IOException
        InputStream mockIs = mock(InputStream.class);
        when(mockIs.read(any(byte[].class), anyInt(), anyInt()))
                .thenThrow(new IOException("Simulated read error"));

        // Expect a BusinessException from the private method
        BusinessException ex = assertThrows(BusinessException.class, () ->
                invokeGetByteFromInputStream(mockIs)
        );
        assertTrue(ex.getMessage().contains("java.io.IOException"),
                "Should wrap the original IOException in BusinessException");
    }

    private byte[] invokeGetByteFromInputStream(InputStream is) throws Exception {
        var method = FileUtility.class.getDeclaredMethod("getByteFromInputStream", InputStream.class);
        method.setAccessible(true);
        try {
            return (byte[]) method.invoke(null, is);
        } catch (Exception e) {
            if (e.getCause() != null) {
                throw (Exception) e.getCause();
            } else {
                throw e;
            }
        }
    }
}
