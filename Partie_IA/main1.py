from flask import Flask, request, jsonify
import pandas as pd
from sqlalchemy import create_engine
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.feature_extraction.text import TfidfVectorizer

app = Flask(__name__)

# Configuration de la base de données
db_url = "mysql+pymysql://root:yNrXkjOdFVPfNoUqmxdGBlYWCuEfEOVV@caboose.proxy.rlwy.net:14050/railway"

def get_recommendations(user_id, db_url, top_n=10):
    # Connexion à la base de données
    engine = create_engine(db_url)
    
    # Récupération des données avec les bonnes requêtes SQL
    query_reviews = """
        SELECT r.user_id, r.book_id, r.rating, b.title, b.author, b.description, b.cover_url 
        FROM reviews r
        JOIN book b ON r.book_id = b.id
    """
    query_books = "SELECT id as book_id, title, author, description, cover_url FROM book"
    
    reviews = pd.read_sql(query_reviews, engine)
    books = pd.read_sql(query_books, engine)
    
    # Vérification si l'utilisateur existe
    user_reviews = reviews[reviews['user_id'] == user_id]
    if user_reviews.empty:
        return []  # Retourne une liste vide si l'utilisateur n'existe pas ou n'a pas de reviews
    
    # Vectorisation des descriptions de livres
    tfidf = TfidfVectorizer(stop_words='english')
    books['description'] = books['description'].fillna('')
    tfidf_matrix = tfidf.fit_transform(books['description'])
    
    # Calcul de la similarité cosinus
    cosine_sim = cosine_similarity(tfidf_matrix, tfidf_matrix)
    
    # Obtenir les indices des livres déjà notés par l'utilisateur
    read_books_indices = [books.index[books['book_id'] == book_id].tolist()[0] 
                         for book_id in user_reviews['book_id']]
    
    # Calcul des scores de recommandation
    sim_scores = []
    for idx in read_books_indices:
        sim_scores.extend(list(enumerate(cosine_sim[idx])))
    
    # Trier les livres par score de similarité
    sim_scores = sorted(sim_scores, key=lambda x: x[1], reverse=True)
    
    # Obtenir les indices des livres recommandés (en excluant ceux déjà lus)
    recommended_indices = [i[0] for i in sim_scores 
                          if i[0] not in read_books_indices][:top_n]
    
    # Retourner les livres recommandés avec cover_url
    recommended_books = books.iloc[recommended_indices][['book_id', 'title', 'author', 'cover_url']]
    return recommended_books.to_dict('records')

@app.route('/recommendations', methods=['GET'])
def recommendations_api():
    user_id = request.args.get('id_user', type=int)
    if not user_id:
        return jsonify({"error": "id_user parameter is required"}), 400
    
    try:
        recommendations = get_recommendations(user_id, db_url, top_n=10)
        return jsonify(recommendations)
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=6000, debug=True)