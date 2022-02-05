import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';
import { withAuthenticator, AmplifySignOut } from '@aws-amplify/ui-react'
import { Amplify, API, graphqlOperation } from 'aws-amplify';
import aws_exports from './aws-exports';
import { createTodo, listTodos, updateTodo, deleteTodo } from './graphql/todo'
Amplify.configure(aws_exports);

class App extends Component {
  
  render() {
  
    const result = await API.graphql(createTodo, {
      input: {
        name: 'My first todo!'
      }
    });
    
    console.log(result);
    const result1 = await API.graphql(listTodos);
    console.log(result1);

    return (
      <div className="App">
        <AmplifySignOut />
        <header className="App-header">
          <img src={logo} className="App-logo" alt="logo" />
          <p>
            Edit <code>src/App.js</code> and save to reload.
          </p>
          <a
            className="App-link"
            href="https://reactjs.org"
            target="_blank"
            rel="noopener noreferrer"
          >
            Learn React
          </a>
        </header>
      </div>
    );
  }
}


export default withAuthenticator(App);
