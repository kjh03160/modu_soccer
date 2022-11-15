package com.modu.soccer.service

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectResult
import com.modu.soccer.TestUtil
import com.modu.soccer.exception.CustomException
import com.modu.soccer.exception.ErrorCode
import org.springframework.mock.web.MockMultipartFile
import spock.lang.Specification

class S3UploadServiceTest extends Specification {
    private S3UploadService service
    private AmazonS3Client client = Mock()



    def setup() {
        service = new S3UploadService(client)
    }

    def "uploadFile"() {
        given:
        def file = TestUtil.getTestImage()
        def result1 = new PutObjectResult()

        1 * client.putObject(_) >> result1
        1 * client.getUrl(_, _) >> new URL("http://localhost:8080/url")

        when:
        def result = service.uploadFile(file)

        then:
        noExceptionThrown()
        result != ""
    }

    def "uploadFile - empty file"() {
        given:
        def emptyArray = new byte[0]
        def file = new MockMultipartFile("file", emptyArray)

        0 * client.putObject(_)
        0 * client.getUrl(_, _)

        when:
        def result = service.uploadFile(file)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.INVALID_PARAM
    }
}
