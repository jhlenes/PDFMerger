package pdf;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;

import javax.swing.filechooser.FileSystemView;
import javax.xml.soap.Text;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller
{

    public static final int INITIAL_MERGE_FIELDS = 4;
    public static final int INITIAL_SPLIT_FIELDS = 2;

    private List<TextField> mergeTextFields = new ArrayList<>();
    private List<Button> mergeAddButtons = new ArrayList<>();
    private Map<Integer, File> filesToMerge = new HashMap<>();

    private File fileToSplit;
    private List<TextField[]> splitTextFields = new ArrayList<>();

    private File defaultDirectory = FileSystemView.getFileSystemView().getDefaultDirectory();

    @FXML
    private VBox mergeFieldsContainer;

    @FXML
    private VBox splitFieldsContainer;

    @FXML
    private Button selectFileButton;

    @FXML
    private TextField splitFileField;

    @FXML
    public void initialize()
    {
        for (int i = 0; i < INITIAL_MERGE_FIELDS; i++)
        {
            addFileField();
        }

        for (int i = 0; i < INITIAL_SPLIT_FIELDS; i++)
        {
            addSplitField();
        }

        // Add image to select PDF button in the split tab
        Image imagePDF = new Image(getClass().getResourceAsStream("file-pdf.png"));
        selectFileButton.setGraphic(new ImageView(imagePDF));
    }

    /**
     * Adds a new field for opening a file
     */
    private void addFileField()
    {
        int fieldIndex = mergeTextFields.size();

        // Align all elements relating to one file horizontally with HBox
        HBox hBox = new HBox();
        hBox.setPadding(new Insets(0, 0, 10, 0));
        hBox.setSpacing(10);

        // Top element has padding towards the top and is moved to the right to align with other fields
        if (fieldIndex == 0)
        {
            hBox.setPadding(new Insets(10, 0, 10, 45));
        }

        // Textfield for displaying the file name
        TextField textField = new TextField("No file chosen");
        textField.setId("textField" + fieldIndex);
        mergeTextFields.add(textField);

        // Button to add more PDF files
        Image imagePDF = new Image(getClass().getResourceAsStream("file-pdf.png"));
        Button addFileButton = new Button("Add file");
        addFileButton.setId("openFileButton" + fieldIndex);
        addFileButton.setOnAction(this::addFile);
        addFileButton.setGraphic(new ImageView(imagePDF));
        mergeAddButtons.add(addFileButton);

        // Add textfield and button to HBox
        hBox.getChildren().addAll(textField, addFileButton);
        hBox.setAlignment(Pos.CENTER);

        // Add a button to rearrange the order for all but the top field
        if (fieldIndex > 0)
        {
            Image imageUp = new Image(getClass().getResourceAsStream("up-arrow.png"));
            Button orderButton = new Button();
            orderButton.setId("orderButton" + fieldIndex);
            orderButton.setOnAction(this::reorder);
            orderButton.setGraphic(new ImageView(imageUp));

            hBox.getChildren().add(0, orderButton);
        }

        // Add HBox to surrounding pane
        mergeFieldsContainer.getChildren().add(fieldIndex, hBox);
    }

    /**
     * Adds a new field for splitting a file
     */
    private void addSplitField()
    {
        int fieldIndex = splitTextFields.size();

        // Align all elements relating to one field horizontally with HBox
        HBox hBox = new HBox();
        hBox.setPadding(new Insets(0, 0, 10, 0));
        hBox.setSpacing(10);

        // Top element has padding towards the top and is moved to the right to align with other fields
        if (fieldIndex == 0)
        {
            hBox.setPadding(new Insets(10, 0, 10, 0));
        }

        // Labels
        Label label1 = new Label("From page:");
        Label label2 = new Label("To page:");

        // Textfields for setting page numbers
        TextField textField1 = new TextField("0");
        TextField textField2 = new TextField("0");
        splitTextFields.add(new TextField[]{textField1, textField2});

        // Add textfield and button to HBox
        hBox.getChildren().addAll(label1, textField1, label2, textField2);
        hBox.setAlignment(Pos.CENTER);

        // Add HBox to surrounding pane, below file chooser
        splitFieldsContainer.getChildren().add(fieldIndex + 1, hBox);
    }

    /**
     * Adds a new file
     */
    @FXML
    private void addFile(ActionEvent actionEvent)
    {
        Button button = (Button) actionEvent.getSource();
        int fileIndex = Integer.parseInt(button.getId().substring("openFileButton".length()));

        // Customize and open the filechooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose PDF files to merge");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialDirectory(defaultDirectory);
        List<File> files = fileChooser.showOpenMultipleDialog(mergeFieldsContainer.getScene().getWindow());

        // Add chosen files to a map
        if (files != null)
        {
            int i = fileIndex;
            for (File file : files)
            {
                if (i >= mergeTextFields.size()) addFileField();
                mergeTextFields.get(i).setText(file.getName());
                filesToMerge.put(i, file);
                i++;
                defaultDirectory = file.getParentFile();
            }
        } else
        {
            mergeTextFields.get(fileIndex).setText("No file chosen");
            filesToMerge.remove(fileIndex);
        }
    }

    /**
     * Select PDF file for splitting
     */
    @FXML
    private void selectFile(ActionEvent actionEvent)
    {
        // Customize and open the filechooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose PDF file to split");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialDirectory(defaultDirectory);
        File file = fileChooser.showOpenDialog(mergeFieldsContainer.getScene().getWindow());

        if (file != null)
        {
            fileToSplit = file;
            splitFileField.setText(fileToSplit.getName());
        } else
        {
            fileToSplit = null;
            splitFileField.setText("No file chosen");
        }
    }


    /**
     * Called by the "More files" button.
     * Adds a new field for opening a file.
     */
    @FXML
    private void addMergeField(ActionEvent actionEvent)
    {
        addFileField();
    }

    /**
     * Called by the "Add range" button.
     * Adds a new range field for splitting.
     */
    @FXML
    private void addSplitField(ActionEvent actionEvent)
    {
        addSplitField();
    }

    /**
     * Merge the PDF files in the correct order
     */
    @FXML
    private void merge(ActionEvent actionEvent)
    {
        if (filesToMerge.size() < 2)
        {
            showAlert("Error", "Not enough files specified",
                    "You need to specify two or more files in order to merge.");
            return;
        }

        // Customize filechooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save merged document");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialDirectory(defaultDirectory);
        fileChooser.setInitialFileName("merged-document.pdf");

        // Show save file dialog
        File fileToSave = fileChooser.showSaveDialog(mergeFieldsContainer.getScene().getWindow());
        if (fileToSave == null) return; // User exited the dialog

        // Merge files
        PDFMergerUtility mergerUtility = new PDFMergerUtility();
        for (int i = 0; i < mergeTextFields.size(); i++)
        {
            File file = filesToMerge.get(i);
            if (file == null) break;
            try
            {
                mergerUtility.addSource(file);
            } catch (FileNotFoundException e)
            {
                showAlert("Error", "File not found",
                        "The following file could not be found: " + file.getAbsolutePath());
                return;
            }
        }

        mergerUtility.setDestinationFileName(fileToSave.getAbsolutePath());
        try
        {
            mergerUtility.mergeDocuments(null);
            Desktop.getDesktop().open(fileToSave.getParentFile());
        } catch (IOException e)
        {
            showAlert("Error", "Something went wrong", e.getMessage());
            return;
        }
    }

    @FXML
    private void split(ActionEvent actionEvent)
    {
        if (fileToSplit == null)
        {
            showAlert("Error", "No file specified",
                    "You need to specify a file in order to split");
            return;
        }

        // Customize directorychooser
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(fileToSplit.getParentFile());
        directoryChooser.setTitle("Choose directory to save the splitted documents");
        File directory = directoryChooser.showDialog(splitFieldsContainer.getScene().getWindow());
        if (directory == null) return; // User exited the dialog

        // Split file
        for (TextField[] range : splitTextFields)
        {
            try
            {
                int startPage = Integer.parseInt(range[0].getText());
                int endPage = Integer.parseInt(range[1].getText());
                if (startPage == 0 && endPage == 0) continue;
                if (endPage < startPage)
                {
                    showAlert("Error", "End page is less than start page", "Check your input.");
                    return;
                }

                String fileName = directory.getAbsolutePath() + File.separator +
                        fileToSplit.getName().replace(".pdf", "_p" + startPage + "-p" + endPage + ".pdf");

                Splitter splitter = new Splitter();
                splitter.setStartPage(startPage);
                splitter.setEndPage(endPage);
                splitter.setSplitAtPage(endPage); // This is so that the splitted document can hold up to "endPage" amount of pages, default is 1
                PDDocument document = PDDocument.load(fileToSplit);
                List<PDDocument> splitted = splitter.split(document);
                splitted.get(0).save(fileName);
                splitted.get(0).close();
                document.close();
                Desktop.getDesktop().open(directory);
            } catch (NumberFormatException e)
            {
                showAlert("Error", "Input is not a number", e.getMessage());
                return;
            } catch (IndexOutOfBoundsException e)
            {
                showAlert("Error", "Page numbers outside of document range",
                        "Check your input, or see if you chose the right PDF document.");
                return;
            } catch (Exception e)
            {
                showAlert("Error", "Something went wrong", e.getMessage());
                return;
            }
        }
    }

    /**
     * Called by the Up arrow button.
     * Rearranges the order of the PDF files.
     */
    private void reorder(ActionEvent actionEvent)
    {
        Button button = (Button) actionEvent.getSource();
        int fileIndex = Integer.parseInt(button.getId().substring("orderButton".length()));

        // Temporary store the file above
        File file = filesToMerge.get(fileIndex - 1);
        String fileName = mergeTextFields.get(fileIndex - 1).getText();

        // Replace the file above with file on this line
        filesToMerge.put(fileIndex - 1, filesToMerge.get(fileIndex));
        mergeTextFields.get(fileIndex - 1).setText(mergeTextFields.get(fileIndex).getText());

        // Replace file on this line
        filesToMerge.put(fileIndex, file);
        mergeTextFields.get(fileIndex).setText(fileName);
    }

    private void showAlert(String title, String headerText, String contentText)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR, contentText);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.showAndWait();
    }

}
