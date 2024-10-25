import React, { useEffect, useState } from "react";
import axios from "axios";
import "./App.css";
const App = () => {
  const [files, setFiles] = useState([]);
  const [rules, setRules] = useState([]);
  const [selectedFile, setSelectedFile] = useState(null);
  const [newRule, setNewRule] = useState("");
  const [result, setResult] = useState([]);
  const [showResult, setShowResult] = useState(false);
  useEffect(() => {
    fetchFiles();
    fetchRules();
  }, []);

  const fetchFiles = async () => {
    const response = await axios.get("/api/csv/all");
    setFiles(response.data);
  };

  const fetchRules = async () => {
    const response = await axios.get("/api/rule/all");
    setRules(response.data);
  };

  const uploadFile = async (event) => {
    event.preventDefault();
    const formData = new FormData();
    formData.append("file", selectedFile);

    await axios.post("/api/csv/upload", formData);
    setSelectedFile(null);
    fetchFiles();
  };

  const deleteFile = async (fileId) => {
    await axios.delete(`/api/csv/${fileId}`);
    fetchFiles();
  };

  const applyRule = async (ruleId, fileId) => {
    const { data } = await axios.post(`/api/rule/apply/${ruleId}/${fileId}`);
    setResult(data);
    alert(`Rule with id ${ruleId} applied successfully on this file`)
  };

  const createRule = async (e) => {
    try {
      e.preventDefault();
      await axios.post("/api/rule/create", { ruleString: newRule });
      setNewRule("");
      fetchRules();
    } catch (error) {
      alert(error.response.data.message);
    }
  };
  return (
    <div className="app">
      {files.length === 0 && (
        <div className="upload-section">
          <div className="custom-file-input">
            <input
              type="file"
              id="file"
              accept=".csv"
              className="file-input"
              onChange={(e) => setSelectedFile(e.target.files[0])}
            />
            <label htmlFor="file" className="file-label">
              Select File
            </label>
            <span className="file-name">
              {selectedFile === null ? "No file chosen" : selectedFile.name}
            </span>
          </div>
          <button onClick={(e) => uploadFile(e)}>Upload</button>
        </div>
      )}
      {files.length > 0 && (
        <div className="rule-engine">
          <div className="rule-engine1">
            <div className="file">
              <h2>Uploaded File: {files[0].fileName}</h2>
              <button onClick={() => deleteFile(files[0].fileId)}>
                Delete
              </button>
            </div>
            <div className="rule-engine11">
              <p>Available Column Names:</p>
              {files[0].columnNames.map((column, i) => (
                <p key={column}>
                  {column}
                  {i != files[0].columnNames.length - 1 ? " ," : " ."}
                </p>
              ))}
            </div>
          </div>

          <div className="rule-engine2">
            <h2>Available Rules</h2>
            <div className="rule-engine21">
              <div className="rules">
                <div>
                  <p>Rules</p>
                </div>
                <div>
                  <p>Action</p>
                </div>
              </div>
              {rules.length > 0 &&
                rules.map((rule) => (
                  <div className="rules" key={rule.id}>
                    <div>
                      <p>{rule.ruleString}</p>
                    </div>
                    <div>
                      <button
                        onClick={() => applyRule(rule.id, files[0].fileId)}
                      >
                        Apply Rule
                      </button>
                    </div>
                  </div>
                ))}
            </div>
            <div
              className={
                result.length > 0 ? "showResultDiv" : "disableShowResultDiv"
              }
            >
              <p onClick={() => setShowResult(!showResult)}>show result</p>
            </div>
          </div>
          <div className="create-new-rule">
            <input
              type="text"
              value={newRule}
              onChange={(e) => setNewRule(e.target.value)}
              placeholder="Create New Rule"
            />
            <button onClick={(e) => createRule(e)}>Create</button>
          </div>
        </div>
      )}
      {showResult === true && (
        <div className="result">
          <div className="resultDiv">
            <div className="close">
              <p onClick={() => setShowResult(false)}>close</p>
              <h2>Result</h2>
            </div>
            <div className="table-container">
              <table className="simple-table">
                <thead>
                  <tr>
                    {files[0].columnNames.map((columnName, index) => (
                      <th key={index}>{columnName}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {result.map((row, rowIndex) => (
                    <tr key={rowIndex}>
                      {files[0].columnNames.map((columnName, colIndex) => (
                        <td key={colIndex}>{row[columnName]}</td>
                      ))}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default App;
