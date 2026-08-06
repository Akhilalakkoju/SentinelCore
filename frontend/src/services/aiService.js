const API_URL = "http://127.0.0.1:8000/chat";

export async function askAI(question) {
    try {
        const response = await fetch(API_URL, {
            method: "POST",
            headers: {
            
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                question: question
            })
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error("AI API Error:", response.status, errorText);
            throw new Error('AI service returned ${response.status}');
        }

        const data = await response.json();

        console.log("AI Response:", data);

        return data;

    } catch (error) {
        console.error("Failed to contact AI:", error);
        throw error;
    }
}