import org.apache.poi.xwpf.usermodel.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class MyApp {
    private static Random random = new Random();
    private static Connection connection;
    private static PreparedStatement preparedStatement;
    private static String CREATE_ADVERT = "insert into advertisement" +
            "(title, text, date, categoryId, price, currency, userId, status, id) values" +
            "(?,?,?,?,?,?,?,?,?);";
    private static String CREATE_CATEGORY = "insert into category" +
            "(id, category, parentId) values (?,?,?)";
    private static String CREATE_USER = "insert into user" +
            "(id, name, email, password, phone, status, type, dislikeAmount, picture ) values" +
            "(?,?,?,?,?,?,?,?,?)";
    private static String CREATE_SUB_CATEGORY = "insert into category(category, parentId) values (?,?)";
    private static String LINK_IMAGES = "insert into adpicture" +
            "(id, picture, adId, type) values (?,?,?,?)";

    private static String DELETE_USERS = "DELETE FROM user WHERE id > 0;";
    private static String DELETE_CATEGORY = "DELETE FROM category WHERE id > 0;";
    private static String DELETE_ADVERT = "DELETE FROM advertisement WHERE id > 0;";

    private static String UPDATE_USER_ID = "SET  @num := 0;\n" +
            "UPDATE salesa.user SET id = @num := (@num+1);\n" +
            "ALTER TABLE salesa.user AUTO_INCREMENT =1;";
    private static String UPDATE_CATEGORY_ID = "SET  @num := 0;\n" +
            "UPDATE salesa.category SET id = @num := (@num+1);\n" +
            "ALTER TABLE salesa.category AUTO_INCREMENT =1;";
    private static String UPDATE_ADVERTISEMENT_ID = "SET  @num := 0;\n" +
            "UPDATE salesa.advertisement SET id = @num := (@num+1);\n" +
            "ALTER TABLE salesa.advertisement AUTO_INCREMENT =1;";


    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        XWPFDocument advertDoc = new XWPFDocument(new FileInputStream("ADVERT_LIST.docx"));
        XWPFDocument categoryDoc = new XWPFDocument(new FileInputStream("CATEGORY_LIST.docx"));

        delete(DELETE_USERS);
//        delete(DELETE_CATEGORY);
//        delete(DELETE_ADVERT);

        createUser();
        createCategory(categoryDoc);
        createAdvert(advertDoc);

        linkImagesToAdvert(advertDoc);

//        update(UPDATE_USER_ID);
//        update(UPDATE_CATEGORY_ID);
//        update(UPDATE_ADVERTISEMENT_ID);
    }

    public static void createCategory(XWPFDocument docx) throws SQLException, ClassNotFoundException {
        connection = DataSource.getConnection();
        preparedStatement = connection.prepareStatement(CREATE_CATEGORY);
        List<XWPFTable> tables = docx.getTables();
        XWPFTable xwpfTable = tables.get(0);

        for (int i = 0; i < xwpfTable.getRows().size(); i++) {
            XWPFTableRow row = xwpfTable.getRow(i);
            XWPFTableCell cell = row.getCell(1);
            preparedStatement.setInt(1, i + 1);
            preparedStatement.setString(2, cell.getText());
            preparedStatement.setInt(3, 0);
            preparedStatement.executeUpdate();
        }
        // Implement sub category query
        preparedStatement = connection.prepareStatement(CREATE_SUB_CATEGORY);

        for (int i = 0; i < xwpfTable.getRows().size(); i++) {
            XWPFTableRow row = xwpfTable.getRow(i);
            XWPFTableCell tableCell = row.getCell(2);
            //Split by UpperCase
            String[] subCategoryArray = tableCell.getText().split("(?<=.)(?=\\p{Lu})");

            for (String aSubCategoryArray : subCategoryArray) {
                if (!aSubCategoryArray.equals("")) {
                    preparedStatement.setString(1, aSubCategoryArray);
                    preparedStatement.setInt(2, i + 1);
                    preparedStatement.executeUpdate();
                }
            }
        }
    }

    public static void createUser() throws SQLException, ClassNotFoundException {
        //Create random users depends on userCount
        int userCount = 10;
        String[] users = new String[]{"Alex Folkin", "John Tromvell", "Kate Patonik", "Robert Jn. Artur",
                "Mary Ploter", "Nick Perum", "Solter Mirosh", "Jonny Mnimonik", " Rober Dawny", "Linda Burhurt"};
        String[] emails = new String[]{"@live.com", "@gmail.com", "@yandex.ru", "@yahoo.com", "@i.ua", "@bigmir.net"};
        String[] status = new String[]{"U", "B", "A"};
        String[] type = new String[]{"A", "B", "G"};

        connection = DataSource.getConnection();
        preparedStatement = connection.prepareStatement(CREATE_USER);
        //Fill user data
        for (int i = 0; i < userCount; i++) {
            preparedStatement.setInt(1, i + 1);
            preparedStatement.setString(2, users[i]);
            preparedStatement.setString(3, users[i] + emails[random.nextInt(5)]);
            preparedStatement.setString(4, users[i].replace(" ","") + random.nextInt(100) * 128 / 3 + status[random.nextInt(2)]);
            preparedStatement.setString(5, "0-900-0000000");
            preparedStatement.setString(6, status[random.nextInt(2)]);
            preparedStatement.setString(7, type[random.nextInt(2)]);
            preparedStatement.setInt(8, random.nextInt(10));
            preparedStatement.setString(9, "null");
            preparedStatement.executeUpdate();
        }
    }

    public static void createAdvert(XWPFDocument docx) throws SQLException, ClassNotFoundException {
        connection = DataSource.getConnection();
        preparedStatement = connection.prepareStatement(CREATE_ADVERT);

        List<XWPFTable> tables = docx.getTables();
        for (int i = 0; i < tables.size(); i++) {
            XWPFTable table = tables.get(i);
            XWPFTableRow x = table.getRow(0);
            XWPFTableRow x2 = table.getRow(1);
            java.util.Date javaDate = new java.util.Date();
            Date date = new Date(javaDate.getTime());
            int randId = random.nextInt(12) + 1;
            int userId = random.nextInt(3) + 1;

            preparedStatement.setString(1, x.getCell(1).getText());                       //title
            preparedStatement.setString(2, x2.getCell(2).getText());                      //text
            preparedStatement.setDate(3, date);                                           //date
            preparedStatement.setInt(4, Integer.parseInt(x.getCell(3).getText()));        //categoryId
            preparedStatement.setInt(5, random.nextInt(100));                             //price
            preparedStatement.setString(6, "UAH");                                        //currency
            preparedStatement.setInt(7, userId);                                          //userID
            preparedStatement.setString(8, "A");                                          //status
            preparedStatement.setInt(9, i + 1);                                           //userID
            preparedStatement.executeUpdate();
        }
    }

    public static void linkImagesToAdvert(XWPFDocument docx) throws SQLException, ClassNotFoundException {
        extractImages("ADVERT_LIST.docx");

        connection = DataSource.getConnection();
        preparedStatement = connection.prepareStatement(LINK_IMAGES);

        List<XWPFTable> tables = docx.getTables();
        for (int i = 0; i < tables.size(); i++) {
            preparedStatement.setInt(1, i + 1);                                                     // Picture ID
            preparedStatement.setString(2, "src/main/resources/images/image_0" + i + ".jpg");       // Picture Path
            preparedStatement.setInt(3, i + 1);                                                     // Advert ID
            preparedStatement.setString(4, "M");                                                    // Picture Type
            preparedStatement.executeUpdate();
        }

    }

    public static void extractImages(String src) {
        try {
            FileInputStream fs = new FileInputStream(src);
            XWPFDocument docx = new XWPFDocument(fs);
            List<XWPFPictureData> allPictures = docx.getAllPictures();
            Iterator<XWPFPictureData> iterator = allPictures.iterator();

            File imagesFolder = new File("src/main/resources/images");
            imagesFolder.mkdir();

            int i = 0;
            while (iterator.hasNext()) {
                XWPFPictureData pic = iterator.next();
                byte[] bytepic = pic.getData();
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytepic));
                ImageIO.write(image, "jpg", new File("src/main/resources/images/image_0" + i + ".jpg"));
                i++;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void delete(String deleteQuery) throws SQLException, ClassNotFoundException {
        connection = DataSource.getConnection();
        preparedStatement = connection.prepareStatement(deleteQuery);
        preparedStatement.executeUpdate();
    }

    public static void update(String updateQuery) throws SQLException, ClassNotFoundException {
        connection = DataSource.getConnection();
        preparedStatement = connection.prepareStatement(updateQuery);
        preparedStatement.executeUpdate();
    }
}
