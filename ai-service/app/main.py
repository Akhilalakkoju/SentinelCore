from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

from app.chatbot import ask_ai, analyze_alert_ai

import app.chatbot
print("Loaded chatbot from:", app.chatbot.__file__)

app = FastAPI(title="SentinelCore AI Service")

# Enable CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:5173",
        "http://127.0.0.1:5173",
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# -------------------------------
# Request Models
# -------------------------------

class ChatRequest(BaseModel):
    question: str


class AlertRequest(BaseModel):
    title: str
    severity: str
    description: str
    sourceIp: str


# -------------------------------
# Home
# -------------------------------

@app.get("/")
def home():
    return {
        "status": "running",
        "service": "SentinelCore AI Service"
    }


# -------------------------------
# AI Chat
# -------------------------------

@app.post("/chat")
def chat(request: ChatRequest):
    return {
        "answer": ask_ai(request.question)
    }


# -------------------------------
# AI Alert Analysis
# -------------------------------

@app.post("/analyze-alert")
def analyze_alert(request: AlertRequest):
    return {
        "analysis": analyze_alert_ai(
            request.title,
            request.severity,
            request.description,
            request.sourceIp
        )
    }