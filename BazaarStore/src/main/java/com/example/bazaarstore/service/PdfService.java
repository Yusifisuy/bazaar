package com.example.bazaarstore.service;


import com.example.bazaarstore.dto.product.ProductDTO;
import com.example.bazaarstore.model.entity.Adress;
import com.example.bazaarstore.model.entity.User;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class PdfService {



    public File createProductCreationPdf(ProductDTO productDTO, String fileName) throws IOException, DocumentException {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(fileName));
        document.open();
        document.add(new Paragraph("Your product created :" + productDTO.getName())); // add the object to the document
        document.close();

        return new File(fileName);
    }


    public File makePayment(User user, String fileName, Adress address) throws IOException, DocumentException {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(fileName));
        document.open();
        document.add(new Paragraph(
                "Your have order from :" + user.getUsername() + " to " + address.getCountry() +
                        " " + address.getCity() +" " +  address.getStreet() + " " + user.getPhoneNumber()
        ));
        document.close();

        return new File(fileName);
    }

}
