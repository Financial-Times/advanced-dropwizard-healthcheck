package com.ft.platform.dropwizard.html;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.zip.GZIPInputStream;

public class HTMLWrapperFilter implements Filter {

    private final String applicationName;
    private StringBuilder fileIn = new StringBuilder();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String CHARSET_UTF_8 = ";charset=utf-8";

    public HTMLWrapperFilter(final String applicationName) {
        this.applicationName = applicationName;
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {

        CharResponseWrapper wrappedResponse = new CharResponseWrapper((HttpServletResponse) servletResponse);

        filterChain.doFilter(servletRequest, wrappedResponse);

        byte[] bytes = wrappedResponse.getByteArray();

        wrappedResponse.setContentType(MediaType.TEXT_HTML + CHARSET_UTF_8);

        //Need to override gzip filter if enabled..
        wrappedResponse.setHeader("Content-Encoding", "");

        if (isFileSet()) {


            String unpackedResult = handleFormat(bytes);

            String out = String.format(fileIn.toString(), applicationName, unpackedResult.toString());

            servletResponse.setContentLength(out.getBytes().length);
            servletResponse.getOutputStream().write(out.getBytes());

        } else {
            logger.error("Unable to load template for HTML health check output.");
            wrappedResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private String handleFormat(byte[] bytes) {

        try {
            GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(bytes));

            InputStreamReader reader = new InputStreamReader(gzipInputStream);
            BufferedReader in = new BufferedReader(reader);

            StringBuilder sb = new StringBuilder();
            String readed;
            while ((readed = in.readLine()) != null) {
                sb.append(readed);
            }

            return sb.toString();

        } catch (IOException e) {
            return new String(bytes);
        }

    }

    @Override
    public void destroy() {
    }

    private static class ByteArrayServletStream extends ServletOutputStream {

        private ByteArrayOutputStream baos;

        ByteArrayServletStream(final ByteArrayOutputStream baos) {
            this.baos = baos;
        }

        public void write(final int param) throws IOException {
            baos.write(param);
        }
    }

    private static class ByteArrayPrintWriter {

        private ByteArrayOutputStream baos = new ByteArrayOutputStream();

        private PrintWriter pw = new PrintWriter(baos);

        private ServletOutputStream sos = new ByteArrayServletStream(baos);

        public PrintWriter getWriter() {
            return pw;
        }

        public ServletOutputStream getStream() {
            return sos;
        }

        byte[] toByteArray() {
            return baos.toByteArray();
        }
    }

    private class CharResponseWrapper extends HttpServletResponseWrapper {
        private ByteArrayPrintWriter output;
        private boolean usingWriter;

        public CharResponseWrapper(final HttpServletResponse response) {
            super(response);
            usingWriter = false;
            output = new ByteArrayPrintWriter();
        }

        public byte[] getByteArray() {
            return output.toByteArray();
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            // will error out, if in use
            if (usingWriter) {
                super.getOutputStream();
            }
            usingWriter = true;
            return output.getStream();
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            // will error out, if in use
            if (usingWriter) {
                super.getWriter();
            }
            usingWriter = true;
            return output.getWriter();
        }

        public String toString() {
            return output.toString();
        }
    }

    private boolean isFileSet() {

        if (fileIn.length() > 0) {
            return true;
        }

        InputStream in = ClassLoader.getSystemResourceAsStream("origami.html");

        if (in == null) {
            return false;
        }


        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                fileIn.append(sCurrentLine + "\n");
            }

        } catch (IOException e) {
            logger.error("Unable to load origami configuration file", e);
            return false;
        }

        return true;
    }
}
