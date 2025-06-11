import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class RecommendationsServices {
  private URL = 'http://127.0.0.1:8088'; // Flask API base URL

  constructor(private http: HttpClient) {}

  // Method to get book recommendations for a given user_id
  getRecommendations(userId: number) {
    return this.http.get(`${this.URL}/recommendations?id_user=${userId}`);
  }
}