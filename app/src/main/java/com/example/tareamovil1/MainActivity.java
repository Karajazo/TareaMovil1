package com.example.tareamovil1;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // Componentes UI
    private EditText etTitle, etAuthor, etIsbn, etSearch, etUserId;
    private Button btnAddBook, btnSearch, btnLoan, btnReturn, btnStats;
    private ListView lvBooks;
    private TextView tvStats;

    // Modelos y controladores
    private LibraryManager libraryManager;
    private BookAdapter bookAdapter;
    private int selectedBookPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeUIComponents();
        setupAdapters();
        setupEventListeners();

        libraryManager = new LibraryManager();
        libraryManager.addSampleBooks();
        refreshBookList();
    }

    // Clase interna para manejar la lógica de la biblioteca
    private class LibraryManager {
        private List<Book> books;
        private List<Loan> loans;
        private Map<String, Integer> loanStatistics;

        public LibraryManager() {
            books = new ArrayList<>();
            loans = new ArrayList<>();
            loanStatistics = new HashMap<>();
        }

        public void addSampleBooks() {
            registerBook(new Book("El Principito", "Antoine de Saint-Exupéry", "9788478887194"));
            registerBook(new Book("Cien años de soledad", "Gabriel García Márquez", "9780307474728"));
            registerBook(new Book("1984", "George Orwell", "9780451524935"));
        }

        public void registerBook(Book book) {
            books.add(book);
        }

        public List<Book> searchBooks(String query) {
            List<Book> results = new ArrayList<>();
            String lowerQuery = query.toLowerCase();

            for (Book book : books) {
                if (book.getTitle().toLowerCase().contains(lowerQuery) ||
                        book.getAuthor().toLowerCase().contains(lowerQuery)) {
                    results.add(book);
                }
            }
            return results;
        }

        public void loanBook(String bookId, String userId) throws LibraryException {
            Book book = findBookById(bookId);

            if (book == null) {
                throw new LibraryException("Libro no encontrado");
            }

            if (!book.isAvailable()) {
                throw new LibraryException("El libro ya está prestado");
            }

            Loan newLoan = new Loan(bookId, userId, new Date());
            loans.add(newLoan);
            book.setAvailable(false);

            // Actualizar estadísticas
            String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
            loanStatistics.put(today, loanStatistics.getOrDefault(today, 0) + 1);
        }

        public void returnBook(String bookId) throws LibraryException {
            Book book = findBookById(bookId);

            if (book == null) {
                throw new LibraryException("Libro no encontrado");
            }

            if (book.isAvailable()) {
                throw new LibraryException("Este libro no está prestado");
            }

            for (Loan loan : loans) {
                if (loan.getBookId().equals(bookId) && loan.getReturnDate() == null) {
                    loan.setReturnDate(new Date());
                    book.setAvailable(true);
                    return;
                }
            }

            throw new LibraryException("No se encontró el préstamo");
        }

        public String generateStatisticsReport() {
            if (loanStatistics.isEmpty()) {
                return "No hay estadísticas de préstamos disponibles";
            }

            StringBuilder report = new StringBuilder("Estadísticas de préstamos:\n\n");
            int total = 0;

            for (Map.Entry<String, Integer> entry : loanStatistics.entrySet()) {
                report.append(entry.getKey())
                        .append(": ")
                        .append(entry.getValue())
                        .append(" préstamos\n");
                total += entry.getValue();
            }

            report.append("\nTotal: ").append(total).append(" préstamos");
            return report.toString();
        }

        public List<Book> getAllBooks() {
            return new ArrayList<>(books);
        }

        private Book findBookById(String bookId) {
            for (Book book : books) {
                if (book.getIsbn().equals(bookId)) {
                    return book;
                }
            }
            return null;
        }
    }

    // Clase modelo para Libros
    private static class Book {
        private String title;
        private String author;
        private String isbn;
        private boolean available;

        public Book(String title, String author, String isbn) {
            this.title = title;
            this.author = author;
            this.isbn = isbn;
            this.available = true;
        }

        // Getters y setters
        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public String getIsbn() { return isbn; }
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }

        @Override
        public String toString() {
            return title + " - " + author + " (" + (available ? "Disponible" : "Prestado") + ")";
        }
    }

    // Clase modelo para Préstamos
    private static class Loan {
        private String bookId;
        private String userId;
        private Date loanDate;
        private Date returnDate;

        public Loan(String bookId, String userId, Date loanDate) {
            this.bookId = bookId;
            this.userId = userId;
            this.loanDate = loanDate;
        }

        // Getters y setters
        public String getBookId() { return bookId; }
        public Date getLoanDate() { return loanDate; }
        public Date getReturnDate() { return returnDate; }
        public void setReturnDate(Date returnDate) { this.returnDate = returnDate; }
    }

    // Excepción personalizada para la biblioteca
    private static class LibraryException extends Exception {
        public LibraryException(String message) {
            super(message);
        }
    }

    // Adaptador personalizado para libros
    private class BookAdapter extends ArrayAdapter<String> {
        public BookAdapter(List<String> books) {
            super(MainActivity.this, android.R.layout.simple_list_item_1, books);
        }
    }

    // Métodos de inicialización
    private void initializeUIComponents() {
        etTitle = findViewById(R.id.etTitle);
        etAuthor = findViewById(R.id.etAuthor);
        etIsbn = findViewById(R.id.etIsbn);
        etSearch = findViewById(R.id.etSearch);
        etUserId = findViewById(R.id.etUserId);

        btnAddBook = findViewById(R.id.btnAddBook);
        btnSearch = findViewById(R.id.btnSearch);
        btnLoan = findViewById(R.id.btnLoan);
        btnReturn = findViewById(R.id.btnReturn);
        btnStats = findViewById(R.id.btnStats);

        lvBooks = findViewById(R.id.lvBooks);
        tvStats = findViewById(R.id.tvStats);
    }

    private void setupAdapters() {
        bookAdapter = new BookAdapter(new ArrayList<>());
        lvBooks.setAdapter(bookAdapter);
    }

    private void setupEventListeners() {
        btnAddBook.setOnClickListener(v -> addNewBook());
        btnSearch.setOnClickListener(v -> searchBooks());
        btnLoan.setOnClickListener(v -> processLoan());
        btnReturn.setOnClickListener(v -> processReturn());
        btnStats.setOnClickListener(v -> showStatistics());

        lvBooks.setOnItemClickListener((parent, view, position, id) -> {
            selectedBookPosition = position;
            showToast("Libro seleccionado: " + libraryManager.getAllBooks().get(position).getTitle());
        });
    }

    // Métodos de negocio
    private void addNewBook() {
        String title = etTitle.getText().toString().trim();
        String author = etAuthor.getText().toString().trim();
        String isbn = etIsbn.getText().toString().trim();

        if (title.isEmpty() || author.isEmpty() || isbn.isEmpty()) {
            showToast("Complete todos los campos");
            return;
        }

        libraryManager.registerBook(new Book(title, author, isbn));
        refreshBookList();
        clearForm();
        showToast("Libro agregado");
    }

    private void searchBooks() {
        String query = etSearch.getText().toString().trim();
        List<Book> results = libraryManager.searchBooks(query);

        List<String> displayList = new ArrayList<>();
        for (Book book : results) {
            displayList.add(book.toString());
        }

        bookAdapter.clear();
        bookAdapter.addAll(displayList);
        bookAdapter.notifyDataSetChanged();
    }

    private void processLoan() {
        if (selectedBookPosition == -1) {
            showToast("Seleccione un libro");
            return;
        }

        String userId = etUserId.getText().toString().trim();
        if (userId.isEmpty()) {
            showToast("Ingrese ID de usuario");
            return;
        }

        try {
            Book selected = libraryManager.getAllBooks().get(selectedBookPosition);
            libraryManager.loanBook(selected.getIsbn(), userId);
            refreshBookList();
            showToast("Préstamo registrado");
        } catch (LibraryException e) {
            showToast(e.getMessage());
        }
    }

    private void processReturn() {
        if (selectedBookPosition == -1) {
            showToast("Seleccione un libro");
            return;
        }

        try {
            Book selected = libraryManager.getAllBooks().get(selectedBookPosition);
            libraryManager.returnBook(selected.getIsbn());
            refreshBookList();
            showToast("Libro devuelto");
        } catch (LibraryException e) {
            showToast(e.getMessage());
        }
    }

    private void showStatistics() {
        tvStats.setText(libraryManager.generateStatisticsReport());
    }

    // Métodos utilitarios
    private void refreshBookList() {
        List<String> displayList = new ArrayList<>();
        for (Book book : libraryManager.getAllBooks()) {
            displayList.add(book.toString());
        }

        bookAdapter.clear();
        bookAdapter.addAll(displayList);
        bookAdapter.notifyDataSetChanged();
    }

    private void clearForm() {
        etTitle.setText("");
        etAuthor.setText("");
        etIsbn.setText("");
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}