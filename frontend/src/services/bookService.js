import { api } from "./api.js";

export async function getBooks() {
  const response = await api.get("/books");
  return response.data || [];
}

export async function borrowBook(bookId) {
  return api.post("/books/borrow", { bookId });
}

export async function returnBook(borrowingId) {
  return api.post("/books/return", { borrowingId });
}

export async function getMyBorrowings() {
  const response = await api.get("/books/my-borrowings");
  return response.data || [];
}

export async function getAllBorrowings() {
  const response = await api.get("/librarian/borrowings");
  return response.data || [];
}

export async function returnBorrowingAsLibrarian(borrowingId) {
  return api.post("/librarian/borrowings/return", { borrowingId });
}

export async function addBook(payload) {
  return api.post("/librarian/books", payload);
}

export async function updateBook(payload) {
  return api.post("/librarian/books/update", payload);
}

export async function deleteBook(bookId) {
  return api.post("/librarian/books/delete", { bookId });
}
