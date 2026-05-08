import { getMyBorrowings, returnBook } from "./bookService.js";

export async function getMyLoans() {
  return getMyBorrowings();
}

export async function returnLoan(borrowingId) {
  return returnBook(borrowingId);
}
