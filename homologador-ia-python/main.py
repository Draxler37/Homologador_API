from fastapi import FastAPI
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer, util
import numpy as np
from typing import List

# --- Carga del Modelo de IA ---
# El modelo se descarga y carga en memoria una sola vez al iniciar.
# 'all-MiniLM-L6-v2' es un modelo rápido y eficaz.
# Para español, 'hiiamsid/sentence_similarity_spanish_es' puede dar mejores resultados.
print("Cargando modelo de IA...")
model = SentenceTransformer('all-MiniLM-L6-v2') 
print("Modelo cargado.")

app = FastAPI(
    title="Servicio de IA para Homologación de Autos",
    description="Convierte texto a vectores y calcula similitud."
)

# --- Definición de los datos de entrada/salida ---
class EmbeddingRequest(BaseModel):
    text: str

class EmbeddingResponse(BaseModel):
    text: str
    vector: List[float]

# --- Endpoints de la API ---

@app.post("/embed", response_model=EmbeddingResponse)
def get_embedding(request: EmbeddingRequest):
    """
    Recibe un texto y devuelve su representación vectorial (embedding).
    """
    vector = model.encode(request.text, convert_to_tensor=False).tolist()
    return EmbeddingResponse(text=request.text, vector=vector)

@app.get("/")
def read_root():
    return {"status": "Servicio de IA funcionando"}