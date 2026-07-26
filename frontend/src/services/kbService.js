import api from "./api";

/**
 * Knowledge Base Axios API Integrations
 */
const kbService = {
  
  // Get all articles with search / type filters
  getArticles: async (query = "", type = "All") => {
    const params = {};
    if (query) params.query = query;
    if (type && type !== "All") params.type = type;
    
    const response = await api.get("/kb", { params });
    return response.data;
  },

  // Get article by ID
  getArticleById: async (id) => {
    const response = await api.get(`/kb/${id}`);
    return response.data;
  },

  // Create a new KB article
  createArticle: async (articleData) => {
    const response = await api.post("/kb", articleData);
    return response.data;
  },

  // Update existing KB article (which creates a revision log)
  updateArticle: async (id, articleData) => {
    const response = await api.put(`/kb/${id}`, articleData);
    return response.data;
  },

  // Delete a KB article
  deleteArticle: async (id) => {
    const response = await api.delete(`/kb/${id}`);
    return response.data;
  },

  // Get revision history for an article
  getRevisions: async (id) => {
    const response = await api.get(`/kb/${id}/revisions`);
    return response.data;
  },

  // Restore a specific version
  restoreRevision: async (id, version) => {
    const response = await api.post(`/kb/${id}/revisions/${version}/restore`);
    return response.data;
  },
};

export default kbService;
